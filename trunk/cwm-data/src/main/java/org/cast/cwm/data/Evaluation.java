/*
 * Copyright 2011-2014 CAST, Inc.
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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

/**
 * Information related to scoring of a UserContent or other content object.
 * This may be based on automatic or manual scoring.  There may be zero, one,
 * or more than one Evaluation related to a specific content object.
 */
@Entity
@Audited
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
@Table(indexes={
		@Index(columnList="user_id"),
		@Index(columnList="userContent_id"),
})
@Getter 
@Setter
@ToString(of={"id"})
public class Evaluation extends PersistedObject{

	private static final long serialVersionUID = 1L;
	
	@Id @GeneratedValue(generator = "my_generator")
	@Setter(AccessLevel.NONE) 
	private Long id;

	// TODO some notion of what kind of evaluation this is... automated or manual?  For content or for mechanics?
	// This may be application-specific, or maybe there are some general categories?

	/**
	 * User who did the Evaluation.
	 * May be null if automatically scored.
	 */
	@ManyToOne
	private User user;
	
	/**
	 * UserContent object that is the target of this Evaluation.
	 * May be null if some other type of thing is being evaluated.
	 */
	@ManyToOne
	private UserContent userContent;
	
	@Column(nullable=false)
	private Date createDate;
	
	@Column(nullable=false)
	private Date lastUpdated;
	
	// TODO should there be a text field?  It's reasonably common to evaluate that way.
	
	/**
	 * Overall score recorded for this item.
	 * For single-select multiple choice, this is "1" if a correct answer has been made at any time.
	 * For other responses, it is "1" if the most recent teacher scoring was "Got it".
	 */
	private Integer score;
	
	/**
	 * Number of points actually correct in the most recently-stored answer.
	 * This might be different than the overall score; for instance if a user had gotten a 
	 * multiple-choice question right the first time, and then started exploring other 
	 * possible answers in order to look at the feedback messages, the score would be 1
	 * on the basis of the initial correct answer, but number currently correct is 0.
	 */
	private Integer correct;
	
	/**
	 * Total number of points possible. this is the scale on which the score is measured.
	 * This is 1 for multiple choice and scored free responses;
	 * will get other possible values as more response types are added.
	 */
	private Integer possible;
	
	/**
	 * Number of points actually attempted from among the {@link #total} possible.
	 * This may be null if skipping part of the question is impossible.
	 */
	private Integer attempted;
	
	/**
	 * Number of tries, up to the one recorded as the score for this response.
	 * Eg, tries==1 for a multiple choice that was answered correctly the very first time.
	 */
	private Integer tries;


}
