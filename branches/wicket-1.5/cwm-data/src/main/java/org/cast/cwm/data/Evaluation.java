package org.cast.cwm.data;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;

/**
 * Information related to scoring of a UserContent or other content object.
 * This may be based on automatic or manual scoring.  There may be zero, one,
 * or more than one Evaluation related to a specific content object.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(name="my_generator", strategy = "org.cast.cwm.CwmIdGenerator")
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
	@Index(name="evaluation_user_idx")
	private User user;
	
	/**
	 * UserContent object that is the target of this Evaluation.
	 * May be null if some other type of thing is being evaluated.
	 */
	@Index(name="evaluation_usercontent_idx")
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
