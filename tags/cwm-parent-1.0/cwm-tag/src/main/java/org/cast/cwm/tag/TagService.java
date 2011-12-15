/*
 * Copyright 2011 CAST, Inc.
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
package org.cast.cwm.tag;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.databinder.hib.Databinder;

import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.User;
import org.cast.cwm.service.EventService;
import org.cast.cwm.tag.model.Tag;
import org.cast.cwm.tag.model.TagPlusInt;
import org.cast.cwm.tag.model.Tagging;
import org.hibernate.Query;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * TODO: These methods should return IModels, not Lists of database objects
 * @author jbrookover
 *
 */
public class TagService implements Serializable {
	
	private static TagService instance = new TagService();

    protected static Map<Character,Class<? extends PersistedObject>> typeMap = new HashMap<Character,Class<? extends PersistedObject>>();
    protected static Map<Class<? extends PersistedObject>,Character> classMap = new HashMap<Class<? extends PersistedObject>,Character>();

    protected int maxTagLength = 15; // maximum legal length for tabs - can be modified by application
    
    protected List<String> defaultTags;
    
	private static final long serialVersionUID = 1L;

	public static TagService get() { 
    	return instance;
    }
    
    public static void setInstance(TagService instance) {
    	TagService.instance = instance;
    }
    
    /**
     * Configure Hibernate classes.  To be called from Application's 
     * configureHibernate method.
     * @param c the Hibernate configuration
     */
	public static void configureTagClasses (Configuration c) {
		c.addAnnotatedClass(Tag.class);
		c.addAnnotatedClass(Tagging.class);
	}
    
    /**
     * Define a class to be taggable and set its code letter that will be stored in the database.
     * This method is expected to be called at application startup.
     * @param code
     * @param clazz
     */
    public void configureTaggableClass (Character code, Class<? extends PersistedObject> clazz) {
		typeMap.put(code, clazz);
		classMap.put(clazz, code);
	}
    
    public boolean isTaggable(Class<? extends PersistedObject> clazz) {
    	return classMap.containsKey(clazz);
    }
    
    /** List all tags a User has used.
     * The first time this is called for a User, if they have no tags,
	 * then the default set of tags will be created.
     * @param person The user whose tags will be listed
     * @return List of Tags
     */
	public List<Tag> tagsForUser(User person) {
		return tagsForUser(person, true);
	}
	
	/**
	 * List of tags a User has used.
	 * The first time this is called for a User, if they have no tags,
	 * and if createDefault is set, then the default set of tags will be created.
	 * @param person
	 * @param createDefault
	 * @return List of Tags
	 */
	@SuppressWarnings("unchecked")
	public List<Tag> tagsForUser (User person, boolean createDefault) {
		Session s = Databinder.getHibernateSession();
		List<Tag> list = s.createCriteria(Tag.class)
			.add(Restrictions.eq("user", person))
			.addOrder(Order.asc("name"))
			.setCacheable(true)
			.list();
		if (createDefault && list.isEmpty() && defaultTags != null) {
			// Create default tags.
			for (String name : defaultTags) {
				findTagCreate(person, name);
			}
			return tagsForUser (person, false);
		} else {
			return list;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<TagPlusInt> countTagsForUser (User person) {
		Session s = Databinder.getHibernateSession();
		List<TagPlusInt> list = s.createQuery(
		  "SELECT new org.cast.cwm.tag.model.TagPlusInt(t, count(tagging)) "
				+ " FROM Tag t LEFT JOIN t.taggings tagging "
			    + " WHERE t.user = :person "
			    + " GROUP BY t.id ")
		  .setParameter("person", person)
		  .list();
		return list;
	}
    
    /** Find all taggings for an object */
    public List<Tagging> taggingsForTarget (User person, PersistedObject target) {
    	return taggingsForTarget (person, target.getClass(), target.getId());
    }
    
    /** Find all taggings for an object */
    @SuppressWarnings("unchecked")
	public List<Tagging> taggingsForTarget(User person, Class<? extends PersistedObject> clazz, Long targetId) {
    	Character type = getTypeCode(clazz);
    	if (type != null) {    	
    		Session session = Databinder.getHibernateSession();
    		return session.createQuery(
    				"from Tagging t " +
    				" where t.targetType = :type " +
    				" and t.targetId = :id " +
    				" and t.tag.user = :person " +
    		" order by t.tag.name ")
    		.setCharacter("type", type)
    		.setLong("id", targetId)
    		.setParameter("person", person)
    		.setCacheable(true)
    		.list();
    	} else {
    		throw new IllegalArgumentException ("Object type not taggable: " + clazz);
    	}
	}

	/** Get the taggable class corresponding to a type code */
    public Class<? extends PersistedObject> getTaggableType (Character c) {
    	return typeMap.get(c); 
    }
    
	/**
	 * Get the type code corresponding to a taggable class.
	 * Takes into account that given class may be a subtype of the registered class.
	 * Throws IllegalArgumentException if the given class is assignable to any taggable class
	 * @param clazz The class to look up.
	 * @return
	 */
	public Character getTypeCode (Class<? extends PersistedObject> clazz) {
		Character tchar = classMap.get(clazz);
		if (tchar != null)
			return tchar;
		for (Entry<Class<? extends PersistedObject>, Character> entry : classMap.entrySet()) {
			if (entry.getKey().isAssignableFrom(clazz))
				return entry.getValue();
		}
		throw new IllegalArgumentException ("Object type not taggable: " + clazz);
	}

    /** Get the actual object referred to by a Tagging. */
	public PersistedObject getTarget(Tagging ting) {
		return (PersistedObject) 
			Databinder.getHibernateSession()
				.load(typeMap.get(ting.getTargetType()), ting.getTargetId());
	}
	
	public Tag findTag (User person, String tagName) {
		String cleanName = cleanTagName(tagName);
		Session session = Databinder.getHibernateSession();
		return (Tag) session.createCriteria(Tag.class)
			.add(Restrictions.eq("name", cleanName).ignoreCase())
			.add(Restrictions.eq("user", person))
			.setCacheable(true)
			.uniqueResult();
	}
	
	/** Find a Tag, creating it if necessary */
	public Tag findTagCreate(User person, String tagName) {
		String cleanName = cleanTagName(tagName);
		Session session = Databinder.getHibernateSession();
		Tag t = (Tag) session.createCriteria(Tag.class)
			.add(Restrictions.eq("name", cleanName).ignoreCase())
			.add(Restrictions.eq("user", person))
			.setCacheable(true)
			.uniqueResult();
		if (t == null) {
			t = new Tag(person, cleanName);
			session.save(t);
			flushChanges();
		}
		return t;
	}
	
	/** Find a Tagging, creating it if necessary */
	public Tagging findTaggingCreate (User person, PersistedObject target, String tagName) {
		return findTaggingCreate (person, target.getClass(), target.getId(), tagName);
	}
	
	public Tagging findTagging (User person, Class<? extends PersistedObject>targetType, Long targetId, String tagName) {
		Tag tag = findTag(person, tagName);
		Character tchar = getTypeCode(targetType);
		Session session = Databinder.getHibernateSession();
		return (Tagging) session.createCriteria(Tagging.class)
			.add(Restrictions.eq("tag", tag))
			.add(Restrictions.eq("targetType", tchar))
			.add(Restrictions.eq("targetId", targetId))
			.setCacheable(true)
			.uniqueResult();
	}
	
	public Tagging findTaggingCreate (User person, Class<? extends PersistedObject>targetType, Long targetId, String tagName) {
		Tagging t = findTagging(person, targetType, targetId, tagName);
		if (t == null) {
			Tag tag = findTagCreate(person, tagName);
			Character tchar = getTypeCode(targetType);
			t = new Tagging(tag, tchar, targetId);
			Session session = Databinder.getHibernateSession();
			session.save(t);
			flushChanges();
			EventService.get().saveEvent("tag:create", "Tag: " + tag.getName() + ", TargetType: " + targetType.getSimpleName() + ", TargetId: " + targetId, null);
		}
		return t;
	}
	
	public void removeTagging(User person, Class<? extends PersistedObject> clazz, Long id, String tagName) {
		Tagging t = findTagging(person, clazz, id, tagName);
		removeTagging(t);
	}
	
	public void removeTagging (Tagging t) {
		Session session = Databinder.getHibernateSession();
		session.delete(t);
		flushChanges();
		EventService.get().saveEvent("tag:delete", "Tag: " + t.getTag().getName() + ", TargetType: " + getTaggableType(t.getTargetType()).getSimpleName() + ", TargetId: " + t.getTargetId(), null);
	}

	public void removeTagging (User person, PersistedObject target, String tagName) {
		removeTagging (person, target.getClass(), target.getId(), tagName);
	}

	/** Turn any string into a normal-form Tag name */
    public static String cleanTagName (String rawName) {
        String name = rawName.trim();
        if ("".equals(name))
            return null;
        StringBuilder sb = new StringBuilder(name.length());
        char[] chrs = name.toCharArray();
        int sz = chrs.length;
        boolean allowSpace = false;
        for (int i = 0; i < sz; i++) {
            char ch = chrs[i];
            if (Character.isLetterOrDigit(ch)) {
                sb.append(ch);
                allowSpace = true;
            } else {
                switch (ch) {
                case '*':   /* Fancy stars translated */
                case '\u2605':
                	sb.append('*');
                	break;
                case ' ':   /* Space variants translated, allowed after start */
                case '_':
                case '-':
                    if (allowSpace)
                        sb.append('_');
                    allowSpace = false;
                    break;
                default:
                	if (!Character.isWhitespace(ch))
                		sb.append(ch);
                }
            }
        }
        if (sb.length() == 0)
        	return null;
        // Still may have a trailing whitespace indicator; remove it
        if (sb.charAt(sb.length()-1) == '_')
        	return sb.substring(0, sb.length()-1);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
	public List<Long> getObjectIdsByTag(Tag tag) {
    	Query q = Databinder.getHibernateSession().getNamedQuery("Tagging.getObjectIdsByTag");
    	q.setParameter("tag", tag);
    	return q.list();
    }
    
    public void deleteTaggingsByTargetId(Long targetId) {
    	Session s = Databinder.getHibernateSession();
    	s.createQuery("delete Tagging where targetId=:targetId").setLong("targetId", targetId).executeUpdate();
    	flushChanges();
    }
    
    // Duplicate of CwmService.flushChanges() - but cwm-data is not a dependency of this module.
    // TODO: Yes it is.  Should swap over.
    @Deprecated
    public void flushChanges() {
		Databinder.getHibernateSession().getTransaction().commit();
		Databinder.getHibernateSession().beginTransaction();
    }
    
	public int getMaxTagLength() {
		return maxTagLength;
	}

	public void setMaxTagLength(int maxTagLength) {
		this.maxTagLength = maxTagLength;
	}

	public List<String> getDefaultTags() {
		return defaultTags;
	}

	public void setDefaultTags(List<String> defaultTags) {
		this.defaultTags = defaultTags;
	}

}
