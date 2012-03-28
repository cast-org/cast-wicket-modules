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
package org.cast.cwm.tag.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.cast.cwm.data.PersistedObject;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.Parameter;

@NamedQueries({
	@NamedQuery(
			name="Tagging.getObjectIdsByTag",
			query="select targetId from Tagging where tag=:tag")
})

@GenericGenerator(name="my_generator", 
	    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", 
	    parameters = {@Parameter(name = "optimizer", value = "pooled"), 
					  @Parameter(name = "increment_size", value = "10"),
	                  @Parameter(name = "sequence_name", value = "tag_id_sequence")
	})
	
@Entity
@Table(name="taggings")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Getter
@Setter
public class Tagging extends PersistedObject {
	
    private static final long serialVersionUID = 1L;
    
    @Id @GeneratedValue(generator = "my_generator")
    protected Long id;
    
    protected char targetType;
    
    @Column(nullable=false)
    @Index(name="taggings_targetid_idx")
    protected Long targetId;
    
    @ManyToOne(optional=false)
    @Index(name="taggings_tag_idx")
    protected Tag tag;
    
	@Column(nullable=false)
	private Date insertTime;
    
    public Tagging (Tag tag, char typeCode, Long targetId) {
    	super();
    	this.targetType = typeCode;
    	this.targetId = targetId;
    	this.tag = tag;
    	insertTime = new Date();
    }
    
    public Tagging () {
    	super();
    }

}
