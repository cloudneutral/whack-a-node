package io.cockroachdb.wan.workload.profile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository {
    boolean isSchemaReady();

    ProfileEntity insertProfile();

    void updateProfile(ProfileEntity profile);

    void updateRandomProfile();

    void deleteProfileById(UUID id);

    void deleteRandomProfile();

    List<ProfileEntity> findAll(int limit);

    Optional<ProfileEntity> findFirst();

    Optional<ProfileEntity> findByNextId(UUID id);

    Optional<ProfileEntity> findByRandomId();

    Optional<ProfileEntity> findById(UUID id);

    void deleteAll();
}
