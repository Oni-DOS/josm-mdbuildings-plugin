package org.openstreetmap.josm.plugins.mdbuildings;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class DataSourceConfig {
    public static final String PROFILES = "profiles";
    public static final String SERVERS = "servers";

    private final ArrayList<DataSourceServer> servers;
    private final ArrayList<DataSourceProfile> profiles;

    private DataSourceProfile currentProfile;

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public DataSourceConfig() {
        this.servers = new ArrayList<>();
        this.profiles = new ArrayList<>();
        load();
    }

    public DataSourceServer getServerByName(String name) {
        return servers.stream()
            .filter(dataSourceServer -> dataSourceServer.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public DataSourceProfile getProfileByName(String serverName, String profileName) {
        return profiles.stream()
            .filter(
                dataSourceProfile -> dataSourceProfile.getDataSourceServerName().equals(serverName)
                    && dataSourceProfile.getName().equals(profileName))
            .findFirst()
            .orElse(null);
    }

    public List<DataSourceServer> getServers() {
        return new ArrayList<>(servers);
    }

    public List<DataSourceProfile> getProfiles() {
        return new ArrayList<>(profiles);
    }

    public DataSourceProfile getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(DataSourceProfile profile) {
        currentProfile = profiles.stream().filter(p -> p.equals(profile)).findFirst().orElse(null);
        save();
    }

    public Collection<DataSourceProfile> getServerProfiles(DataSourceServer server) {
        return profiles
            .stream()
            .filter(p -> p.getDataSourceServerName().equals(server.getName()))
            .collect(Collectors.toList());
    }

    public void addServer(DataSourceServer newServer) {
        validateServer(newServer);
        servers.add(newServer);
        save();
    }


    /**
     * It removes server and all related profiles.
     */
    public void removeServer(DataSourceServer server) {
        new ArrayList<>(profiles)
            .stream()
            .filter(p -> p.getDataSourceServerName().equals(server.getName()))
            .forEach(this::removeProfile);

        servers.remove(server);
        save();
    }

    public void updateServer(DataSourceServer oldServer, DataSourceServer newServer) {
        int index = servers.indexOf(oldServer);
        if (index != -1) {
            // Update related profiles if server name changed
            if (!oldServer.getName().equals(newServer.getName())) {
                profiles.stream()
                    .filter(p -> p.getDataSourceServerName().equals(oldServer.getName()))
                    .forEach(p -> p.setDataSourceServerName(newServer.getName()));
            }
            servers.set(index, newServer);
            save();
        }
    }

    public void addProfile(DataSourceProfile newProfile) {
        validateProfile(newProfile);
        profiles.add(newProfile);
        save();
    }

    public void removeProfile(DataSourceProfile profile) {
        profiles.remove(profile);
        save();
    }

    public void updateProfile(DataSourceProfile oldProfile, DataSourceProfile newProfile) {
        int index = profiles.indexOf(oldProfile);
        if (index != -1) {
            profiles.set(index, newProfile);
            save();
        }
    }

    private void load() {
        servers.clear();
        profiles.clear();
        currentProfile = null;

        String serializedServers = BuildingsSettings.DATA_SOURCE_SERVERS.get();
        servers.addAll(DataSourceServer.fromStringJson(serializedServers));

        String serializedProfiles = BuildingsSettings.DATA_SOURCE_PROFILES.get();
        profiles.addAll(DataSourceProfile.fromStringJson(serializedProfiles));

        List<String> serverAndProfileNames = BuildingsSettings.CURRENT_DATA_SOURCE_PROFILE.get();
        if (serverAndProfileNames.size() != 2) {
            return;
        }
        String currentProfileServerName = serverAndProfileNames.get(0);
        String currentProfileName = serverAndProfileNames.get(1);
        currentProfile = getProfileByName(currentProfileServerName, currentProfileName);
    }

    private void save() {
        String serializedServers = DataSourceServer.toJson(servers).toString();
        BuildingsSettings.DATA_SOURCE_SERVERS.put(serializedServers);

        String serializedProfiles = DataSourceProfile.toJson(profiles).toString();
        BuildingsSettings.DATA_SOURCE_PROFILES.put(serializedProfiles);

        if (currentProfile != null) {
            BuildingsSettings.CURRENT_DATA_SOURCE_PROFILE.put(
                new ArrayList<>(List.of(currentProfile.getDataSourceServerName(), currentProfile.getName()))
            );
        } else {
            BuildingsSettings.CURRENT_DATA_SOURCE_PROFILE.put(null);
        }
    }

    public void bulkUpdate(String serversJson, String profilesJson) {
        servers.clear();
        servers.addAll(DataSourceServer.fromStringJson(serversJson));
        profiles.clear();
        profiles.addAll(DataSourceProfile.fromStringJson(profilesJson));
        save();
    }



    /**
     * Swap data source profile order in collection
     *
     * @param src object to move to dst position
     * @param dst object which will be swapped with src object
     */
    public void swapProfileOrder(DataSourceProfile src, DataSourceProfile dst) {
        assert profiles.contains(src);
        assert profiles.contains(dst);

        int srcIndex = profiles.indexOf(src);
        int dstIndex = profiles.indexOf(dst);

        profiles.set(srcIndex, dst);
        profiles.set(dstIndex, src);

        save();
    }

    public void setProfileVisible(DataSourceProfile profile, boolean value) {
        profile.setVisible(value);
        save();
    }

    private void validateServer(DataSourceServer newServer) throws IllegalArgumentException {
        if (servers.stream().anyMatch(s -> s.getName().equals(newServer.getName()))) {
            throw new IllegalArgumentException("DataSourceServer name must be unique!");
        }
    }

    private void validateProfile(DataSourceProfile newProfile) throws IllegalArgumentException {
        if (profiles.stream().anyMatch(p -> p.getName().equals(newProfile.getName())
            && p.getDataSourceServerName().equals(newProfile.getDataSourceServerName()))) {
            throw new IllegalArgumentException("DataSourceProfile name must be unique per server!");
        }
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(name, listener);
    }
}
