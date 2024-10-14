package io.cockroachdb.wan.workload.profile;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StreamUtils;

@Repository
public class JdbcProfileRepository implements ProfileRepository {
    private static final String JSON_DATA = "[{\"_id\":\"6708193b1d017dca757600d7\",\"index\":0,\"guid\":\"1dc80513-32df-4208-81a7-ae0dacff759e\",\"isActive\":false,\"balance\":\"$1,869.94\",\"picture\":\"http://placehold.it/32x32\",\"age\":22,\"eyeColor\":\"blue\",\"name\":\"Kerr Vaughan\",\"gender\":\"male\",\"company\":\"DYNO\",\"email\":\"kerrvaughan@dyno.com\",\"phone\":\"+1 (836) 574-2726\",\"address\":\"670 Crown Street, Guthrie, Minnesota, 6282\",\"about\":\"Dolor sit irure sint aliqua amet. Duis cillum et nulla non proident ullamco enim. Nostrud eu laborum magna quis et.\\r\\n\",\"registered\":\"2020-12-31T12:40:16 -01:00\",\"latitude\":78.883578,\"longitude\":24.826774,\"tags\":[\"Lorem\",\"elit\",\"est\",\"ullamco\",\"duis\",\"laborum\",\"cillum\"],\"friends\":[{\"id\":0,\"name\":\"Norman Weaver\"},{\"id\":1,\"name\":\"Mcintosh Vang\"},{\"id\":2,\"name\":\"Leigh Kirk\"}],\"greeting\":\"Hello, Kerr Vaughan! You have 8 unread messages.\",\"favoriteFruit\":\"strawberry\"}]";

    private final JdbcTemplate jdbcTemplate;

    public JdbcProfileRepository(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isSchemaReady() {
        return !jdbcTemplate.queryForList(
                "select table_name from [show tables] where table_name='wan_user_profile'").isEmpty();
    }

    @Override
    public ProfileEntity insertProfile() {
        ProfileEntity profile = new ProfileEntity();
        profile.setExpireAt(LocalDateTime.now());
        profile.setVersion(0);
        profile.setProfile(JSON_DATA);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO wan_user_profile (expire_at,payload,version) "
                            + "VALUES (?,?,?) returning id::uuid",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setObject(1, profile.getExpireAt());
            ps.setCharacterStream(2, new StringReader(profile.getProfile()));
            ps.setInt(3, profile.getVersion());
            return ps;
        }, keyHolder);

        UUID id = keyHolder.getKeyAs(UUID.class);
        profile.setId(id);

        return profile;
    }

    @Override
    public void updateProfile(ProfileEntity profile) {
        final String sql = "UPDATE wan_user_profile SET expire_at=?,payload=?,version=? "
                + "WHERE id=? and version=?";

        int rows = jdbcTemplate.update(sql,
                ps -> {
                    ps.setObject(1, profile.getExpireAt());
                    ps.setCharacterStream(2, new StringReader(profile.getProfile()));
                    ps.setInt(3, profile.getVersion());
                    ps.setObject(4, profile.getId());
                    ps.setInt(5, profile.getVersion());
                });

        if (rows != 1) {
            throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(sql, 1, rows);
        }
    }

    @Override
    public void updateRandomProfile() {
        findByRandomId().ifPresent(this::updateProfile);
    }

    @Override
    public void deleteProfileById(UUID id) {
        jdbcTemplate.update("DELETE from wan_user_profile WHERE id=? and version=0",
                ps -> {
                    ps.setObject(1, id);
                });
    }

    @Override
    public void deleteRandomProfile() {
        findByRandomId().ifPresent(profileEntity -> deleteProfileById(profileEntity.getId()));
    }

    @Override
    public List<ProfileEntity> findAll(int limit) {
        return jdbcTemplate.query("SELECT * FROM wan_user_profile limit " + limit,
                userProfileRowMapper());
    }

    @Override
    public Optional<ProfileEntity> findFirst() {
        return jdbcTemplate
                .query("SELECT * FROM wan_user_profile order by id limit 1",
                        userProfileRowMapper())
                .stream()
                .findFirst();
    }

    @Override
    public Optional<ProfileEntity> findByNextId(UUID id) {
        return jdbcTemplate
                .query("SELECT * FROM wan_user_profile where id > ? order by id limit 1",
                        userProfileRowMapper(), id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<ProfileEntity> findByRandomId() {
        return jdbcTemplate
                .query("SELECT * FROM wan_user_profile ORDER BY random() limit 1",
                        userProfileRowMapper())
                .stream()
                .findFirst();
    }

    @Override
    public Optional<ProfileEntity> findById(UUID id) {
        return jdbcTemplate
                .query("SELECT * FROM wan_user_profile "
                                + "WHERE (id,version) IN (?,?)",
                        userProfileRowMapper(),
                        id, 0)
                .stream()
                .findFirst();
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.execute("delete from wan_user_profile where 1=1");
    }

    public static final Calendar tzUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    private RowMapper<ProfileEntity> userProfileRowMapper() {
        return (rs, rowNum) -> {
            ProfileEntity profile = new ProfileEntity();
            profile.setId(rs.getObject("id", UUID.class));
            profile.setVersion(rs.getInt("version"));

            Timestamp ts = rs.getTimestamp("expire_at", tzUTC);
            profile.setExpireAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts.getTime()), ZoneOffset.UTC));

            try {
                String payload = StreamUtils.copyToString(
                        rs.getBinaryStream("payload"), Charset.defaultCharset());
                profile.setProfile(payload);
            } catch (IOException e) {
                throw new SQLException(e);
            }

            return profile;
        };
    }

}
