/*
 * This file is part of LCMC written by Rasto Levrinc.
 *
 * Copyright (C) 2014, Rastislav Levrinc.
 *
 * The LCMC is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * The LCMC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LCMC; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package lcmc.cluster.service.storage;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import lcmc.ClusterEventBus;
import lcmc.HwEventBus;
import lcmc.cluster.domain.Cluster;
import lcmc.common.domain.util.Tools;
import lcmc.event.CommonFileSystemsChangedEvent;
import lcmc.event.FileSystemsChangedEvent;
import lcmc.event.HwFileSystemsChangedEvent;
import lcmc.host.domain.Host;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

@Named
@Singleton
public class FileSystemService {
    @Inject
    private HwEventBus hwEventBus;
    @Inject
    private ClusterEventBus clusterEventBus;
    private Map<Host, Set<String>> fileSystemsByHost = new ConcurrentHashMap<Host, Set<String>>();
    private Map<Cluster, Set<String>> commonFileSystemsByCluster = new ConcurrentHashMap<Cluster, Set<String>>();

    public void init() {
        hwEventBus.register(this);
    }

    @Subscribe
    public void onFileSystemsChanged(final HwFileSystemsChangedEvent event) {
        fileSystemsByHost.put(event.getHost(), event.getFileSystems());
        clusterEventBus.post(new FileSystemsChangedEvent(event.getHost(), event.getFileSystems()));
                updateCommonFileSystems(Optional.fromNullable(event.getHost().getCluster()));
    }

    public Set<String> getCommonFileSystems(final Cluster cluster) {
        final Set<String> fileSystems = commonFileSystemsByCluster.get(cluster);
        if (fileSystems == null) {
            return new TreeSet<String>();
        }
        return fileSystems;
    }

    public Set<String> getFileSystems(final Host host) {
        return fileSystemsByHost.get(host);
    }

    private Set<String> getCommonFileSystems(final Collection<Host> hosts) {
        Optional<Set<String>> fileSystemsIntersection = Optional.absent();

        for (final Host host : hosts) {
            final Set<String> fileSystems = fileSystemsByHost.get(host);
            fileSystemsIntersection = Tools.getIntersection(
                    Optional.fromNullable(fileSystems),
                    fileSystemsIntersection);
        }
        return fileSystemsIntersection.or(new TreeSet<String>());
    }

    private void updateCommonFileSystems(final Optional<Cluster> cluster) {
        if (!cluster.isPresent()) {
            return;
        }
        final Set<String> commonFileSystems = getCommonFileSystems(cluster.get().getHosts());
        final Set<String> oldCommonFileSystems = commonFileSystemsByCluster.get(cluster.get());
        commonFileSystemsByCluster.put(cluster.get(), commonFileSystems);
        if (oldCommonFileSystems == null
                || oldCommonFileSystems.isEmpty()
                || !Tools.equalCollections(commonFileSystems, oldCommonFileSystems)) {
            clusterEventBus.post(new CommonFileSystemsChangedEvent(cluster.get(), commonFileSystems));
        }
    }
}
