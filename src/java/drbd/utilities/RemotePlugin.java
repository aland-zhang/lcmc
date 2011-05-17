/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009, LINBIT HA-Solutions GmbH.
 *
 * DRBD Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * DRBD Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with drbd; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */


package drbd.utilities;

import drbd.gui.resources.Info;

import java.util.List;

/**
 * Interface for remote plugins. IMPORTANT: Remote plugins can't have
 * anonymous inner classes.
 */
public interface RemotePlugin {
    /** Init plugin. */
    void init();
    /** Shows description of this plugin. */
    void showDescription();
    /** Adds menu items to the object. */
    void addPluginMenuItems(final Info info, final List<UpdatableItem> items);
}