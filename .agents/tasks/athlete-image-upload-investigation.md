# Athlete Image Upload — Investigation Report

## Summary Answer

The images **are** saved to disk correctly. The bug is that the URL stored in the database (and returned to the client) is a **raw relative filesystem path** (e.g. `athletes/<userId>/profile/<uuid>.jpg`) rather than a fully-qualified, browser-accessible URL (e.g. `http://localhost:8082/storage/athletes/<userId>/profile/<uuid>.jpg`). Because the client receives a relative path it cannot resolve, the image appears missing even though the bytes are physically on disk. The same bug affects both `profileImageUrl` and `iconImageUrl`.

---

## Evidence

### 1. Controller — upload endpoints are correct

**File:** `src/main/java/com/feadev/eaids/registrations/controller/AthleteController.java`  
Lines 86–102

```java
@PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<AthleteProfileResponse> uploadProfileImage(
        @RequestParam("file") MultipartFile file) { ... }

@PostMapping(value = "/me/icon-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<AthleteProfileResponse> uploadIconImage(
        @RequestParam("file") MultipartFile file) { ... }
```

The controller correctly receives the multipart file and delegates to `AthleteService`. No issue here.

---

### 2. Service — the file IS saved, but the wrong value is stored in the DB

**File:** `src/main/java/com/feadev/eaids/registrations/service/impl/AthleteServiceImpl.java`

#### `uploadProfileImage` (lines ~245–260)
```java
String storedPath = storageService.store(file, "athletes/" + userId + "/profile");
profile.setProfileImageUrl(storedPath);   // ← BUG: storedPath is a relative path
AthleteProfile savedProfile = athleteProfileRepository.save(profile);
```

#### `uploadIconImage` (lines ~265–280)
```java
String storedPath = storageService.store(file, "athletes/" + userId + "/icon");
profile.setIconImageUrl(storedPath);      // ← BUG: storedPath is a relative path
AthleteProfile savedProfile = athleteProfileRepository.save(profile);
```

`storageService.store()` returns a **relative path** (e.g. `athletes/<uuid>/profile/<uuid>.png`). That raw relative path is saved directly to the database column and subsequently returned to the client in `AthleteProfileResponse.profileImageUrl` / `iconImageUrl`. A browser has no way to resolve this path.

---

### 3. StorageService — confirms `store()` returns a relative path

**File:** `src/main/java/com/feadev/eaids/registrations/service/impl/LocalStorageServiceImpl.java`

```java
// File IS physically written here:
try (InputStream inputStream = file.getInputStream()) {
    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
}

// Return value is a relative path, not a URL:
String relativePath = basePath.relativize(targetPath).toString();
log.debug("File stored at: {}", relativePath);
return relativePath;   // e.g. "athletes/<userId>/profile/<uuid>.jpg"
```

The storage write itself is correct. The problem is entirely in how the caller uses the return value.

---

### 4. WebMvcConfig — the static resource handler needs the `/storage/` prefix

**File:** `src/main/java/com/feadev/eaids/config/WebMvcConfig.java`

```java
@Value("${storage.local.base-path:./storage}")
private String storageBasePath;

registry.addResourceHandler("/storage/**")
        .addResourceLocations("file:" + absolutePath + "/")
        .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic());
```

Spring serves files from the storage root under the `/storage/**` URL prefix. So the correct URL for a stored file is:

```
http://<host>:<port>/storage/<relative-path-returned-by-store()>
```

---

### 5. application.properties — confirms base URL

```properties
app.base-url=http://localhost:8082
storage.local.base-path=./storage
```

A correct absolute URL for a profile image would be:  
`http://localhost:8082/storage/athletes/<userId>/profile/<uuid>.jpg`

---

### 6. The correct pattern already exists in EventServiceImpl

**File:** `src/main/java/com/feadev/eaids/event/service/impl/EventServiceImpl.java`  
Lines ~79–83

```java
String storedPath = storageService.store(image, "events/" + saved.getId() + "/image");
saved.setImagePath(buildAbsoluteImageUrl(storedPath));   // ← CORRECT: builds full URL
```

`buildAbsoluteImageUrl` (lines ~258–263):

```java
private String buildAbsoluteImageUrl(String relativePath) {
    String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
    return base + "/storage/" + path;
}
```

`EventServiceImpl` injects `@Value("${app.base-url}") private String baseUrl` and calls this helper. `AthleteServiceImpl` does not do this — it is missing both the `@Value("${app.base-url}")` injection and the URL-building step.

A similar gap exists in `TeamServiceImpl`:
```java
// teams/service/impl/TeamServiceImpl.java line ~185-187
String storedPath = storageService.store(file, "teams/" + teamId + "/logo");
team.setLogoUrl(storedPath);   // ← same bug: relative path stored
```

---

### 7. Existing test confirms this is a known pattern requirement

**File:** `src/test/java/com/feadev/eaids/event/service/impl/EventServiceImageUrlBugConditionTest.java`

The test class explicitly documents:
> "Bug Condition: When an image is uploaded (create or update), the system stores only the relative path returned by `StorageService.store()` instead of constructing an absolute URL by prepending the configured base URL."

This same condition applies identically to `AthleteServiceImpl`.

---

## Root Cause

**`AthleteServiceImpl.uploadProfileImage()` and `uploadIconImage()`** store the raw relative path returned by `storageService.store()` directly into the database, instead of converting it to an absolute URL using the `app.base-url` config property and the `/storage/` prefix that the `WebMvcConfig` resource handler serves.

The file write succeeds. The stored DB value is wrong.

---

## Recommendations

### Fix `AthleteServiceImpl`

1. Inject `app.base-url`:
```java
@Value("${app.base-url}")
private String baseUrl;
```

2. Add the same URL-builder helper that `EventServiceImpl` already has:
```java
private String buildAbsoluteImageUrl(String relativePath) {
    String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
    return base + "/storage/" + path;
}
```

3. Use it in both upload methods:
```java
// uploadProfileImage
String storedPath = storageService.store(file, "athletes/" + userId + "/profile");
profile.setProfileImageUrl(buildAbsoluteImageUrl(storedPath));  // was: storedPath

// uploadIconImage
String storedPath = storageService.store(file, "athletes/" + userId + "/icon");
profile.setIconImageUrl(buildAbsoluteImageUrl(storedPath));     // was: storedPath
```

4. When deleting an old image, extract the relative path before calling `storageService.delete()` (mirroring `EventServiceImpl.extractRelativePath()`), otherwise the delete call will pass an absolute URL to `LocalStorageServiceImpl` which resolves it against `basePath` and will fail to find the file. Add an equivalent `extractRelativePath` helper.

### Also fix `TeamServiceImpl`

`TeamServiceImpl` line ~186 has the identical bug — it stores a relative path in `team.setLogoUrl(storedPath)`. Apply the same fix.

### Consider centralising the URL-building logic

Both `EventServiceImpl` and `AthleteServiceImpl` (after fixing) would duplicate this helper. Consider moving `buildAbsoluteImageUrl` / `extractRelativePath` into `LocalStorageServiceImpl` or a dedicated `StorageUrlBuilder` component, optionally making them part of the `StorageService` interface.

---

## Files Responsible

| File | Issue |
|---|---|
| `src/main/java/com/feadev/eaids/registrations/service/impl/AthleteServiceImpl.java` | Lines ~256–258 and ~276–278: stores relative path instead of absolute URL |
| `src/main/java/com/feadev/eaids/registrations/service/impl/LocalStorageServiceImpl.java` | `store()` returns relative path — correct by contract, callers must convert |
| `src/main/java/com/feadev/eaids/config/WebMvcConfig.java` | Correct — serves `/storage/**` from `storageBasePath` |
| `src/main/resources/application.properties` | Correct — `app.base-url` and `storage.local.base-path` both set |
| `src/main/java/com/feadev/eaids/event/service/impl/EventServiceImpl.java` | Reference implementation of the correct pattern |
