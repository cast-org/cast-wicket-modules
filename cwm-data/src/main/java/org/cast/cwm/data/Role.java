/*
 * Copyright 2011-2017 CAST, Inc.
 *
 * This file is part of the CAST Wicket Modules:
 * see <http://code.google.com/p/cast-wicket-modules>.
 *
 * The CAST Wicket Modules are free software: you can redistribute and/or
 * modify them under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The CAST Wicket Modules are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cast.cwm.data;

import lombok.Getter;

/**
 * <p>
 * The access role for a given {@link User}.  Each role
 * has a rank and it is assumed that a user with a higher
 * rank can assume the role of any lower rank using {@link #subsumes(Role)}.
 * </p>
 * @author jbrookover
 *
 */
public enum Role {
	
    GUEST(0, "GUEST"), STUDENT(1, "STUDENT"), TEACHER(2, "TEACHER"), 
    RESEARCHER(3, "RESEARCHER"), ADMIN(4, "ADMIN");

	public static final String GUEST_ROLENAME = "GUEST";
    public static final String STUDENT_ROLENAME = "STUDENT";
    public static final String TEACHER_ROLENAME = "TEACHER";
    public static final String RESEARCHER_ROLENAME = "RESEARCHER";
    public static final String ADMIN_ROLENAME = "ADMIN";

    @Getter private int rank;
    @Getter private String roleString;
    
    Role (int rank, String roleString) {
        this.rank = rank;
        this.roleString = roleString;
    }

    /**
     * Determines whether a member of the current group can assume the role of
     * the target group <code>target</code>.
     * 
     * @param target is the group the requester would like to assume
     * @return true iff the subsumption relation holds
     */
    public boolean subsumes (Role target) {
        return (rank >= target.rank);
    }

    public static Role nullSafeValueOf (String name) {
        if (name == null)
            return null;
        return valueOf(name);
    }

    /**
     * Attempts to find the Role that corresponds to the given
     * name.  Guaranteed not to thrown an exception.
     * 
     * @param name the name of the Role
     * @return the Role enum, or null if a matching one was not found
     */
    public static Role forRoleString (String name) {
        if (name == null)
            return null;
        name = name.toUpperCase();
        Role g = null;
        try {
            g = valueOf(name);
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        return g;
    }


}
