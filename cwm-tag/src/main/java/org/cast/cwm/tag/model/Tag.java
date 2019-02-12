/*
 * Copyright 2011-2019 CAST, Inc.
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
package org.cast.cwm.tag.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.cast.cwm.data.PersistedObject;
import org.cast.cwm.data.User;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Parameter;

@GenericGenerator(name="my_generator", 
	    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", 
	    parameters = {@Parameter(name = "optimizer", value = "pooled"), 
					  @Parameter(name = "increment_size", value = "10"),
	                  @Parameter(name = "sequence_name", value = "tag_id_sequence")
})
	
/** A Tag is a name used by one person for tagging objects.
 *  The 'global' property is not used yet.
 */
	
@Entity
@Table(name="tags")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Getter
@Setter
public class Tag extends PersistedObject {
	
    private static final long serialVersionUID = 1L;
    
    @Id @GeneratedValue(generator = "my_generator")
    protected Long id;
    
    @NaturalId
    @Column(nullable=false)
    protected String name;
    
    @NaturalId
    @ManyToOne(optional=false)
    protected User user;
    
    @Column(nullable=false)
    protected boolean global;
    
    @OneToMany(mappedBy="tag")
    @OrderBy("insertTime desc")
    protected List<Tagging> taggings;
    
	public Tag(User person, String name) {
    	this(person, name, false);
    }
    
    public Tag(User user, String name, boolean global) {
    	this.user = user;
    	this.name = name;
    	this.global = global;
    }
    
    public Tag() {
    	super();
    }

}
