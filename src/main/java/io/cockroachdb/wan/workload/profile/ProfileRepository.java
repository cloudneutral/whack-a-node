package io.cockroachdb.wan.workload.profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository {
    @Deprecated
    boolean isSchemaReady();

    ProfileEntity insertProfileSingleton();

    List<ProfileEntity> insertProfileBatch();

    void updateProfile(ProfileEntity profile);

    @Deprecated
    void updateRandomProfile();

    void deleteProfileById(UUID id);

    @Deprecated
    void deleteRandomProfile();

    List<ProfileEntity> findAll(int limit);

    Optional<ProfileEntity> findFirst();

    Optional<ProfileEntity> findByNextId(UUID id);

    Optional<ProfileEntity> findByRandomId();

    @Deprecated
    Optional<ProfileEntity> findById(UUID id);

    void deleteAll();
}
