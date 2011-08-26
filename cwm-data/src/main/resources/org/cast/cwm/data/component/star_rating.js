/*
 * Star Rating Widget
 *
 * Can be hooked to a radio group in a form like so:
 * <form>
 * <div class="stars">
 *      <label for="rating-1"><input id="rating-1" name="rating" type="radio" value="1" />1 Star</label>
 *      <label for="rating-2"><input id="rating-2" name="rating" type="radio" value="2" />2 Stars</label>
 *      <label for="rating-3"><input id="rating-3" name="rating" type="radio" value="3" />3 Stars</label>
 *      <label for="rating-4"><input id="rating-4" name="rating" type="radio" value="4" />4 Stars</label>
 *      <label for="rating-5"><input id="rating-5" name="rating" type="radio" value="5" />5 Stars</label>
 *   	<input type="button" value="Submit Rating" />
 * </div>
 * </form>
 *
 * starRating.create('.stars');
 *
 * Clicking on a star will trigger a 'click' event on the <input type="button"... />  It is assumed that
 * this will be an AJAX call.
 *
 * @author mbrambilla
 * @author jbrookover
 *
 */

var starRating = {

  /*
   * Private, default onSave Function.  Called when a user clicks a star.  Can be modified
   * via starRating.setOnSave(fn) to hook in another operation.
   */
  onSave: function() {
     // No Op
  },

  /*
   * Create a star rating using the given element(s).
   */
  create: function(selector) {

    // loop over every element matching the selector
    $(selector).each(function() {

		if ($(this).data("ParsedStars") == true)
			return;
		var $list = $('<div class="starsDiv"></div>');
      	// loop over every radio button in each container
      	$(this)
        	.find('input:radio')
        	.each(function(i) {

				// create a star item
				var rating = $(this).parent().text();
				var id = $(this).attr('id');
				var $item = $('<a href="javascript:void(0);"></a> ')
					.attr('title', rating)
					.html("<span>" + rating + "</span>");   // Wrap with span for better IE6 support (hiding numbers)
				$item.data('for-id', id);
				
				// add behaviors to the star item
				if (!$(this).is(":disabled")) {
					starRating.addHandlers($item);
				} else {
					$item.addClass("disabled");
				}
				
				$list.append($item);
				
				// set default value
				if($(this).is(':checked')) {
					$item.prevAll().andSelf().addClass('rating');
				}
			});

        // Hide the original radio buttons and submit button
        $(this).append($list).find('label').hide();
        $(this).append($list).find('input:button').hide();
        $(this).append($list).find('input:submit').hide();
		$(this).data("ParsedStars", true);
    });
  },

  /*
   * Internal function that adds mouse/click behaviors to stars.
   */
  addHandlers: function(item) {
    $(item).click(function(e) {
      // Handle Star click
      var $star = $(item);
      var $allLinks = $(item).parent();

      // Set the radio button value
      $allLinks
        .parent()
        .find('#' + $star.data('for-id') + ':radio')
        .attr('checked', true);

      // Set the ratings
      $allLinks.children().removeClass('rating');
      $star.prevAll().andSelf().addClass('rating');

      // Activate immediate color change
      $(this).siblings().andSelf().removeClass('rating-over');
      $(this).siblings().andSelf().removeClass('rating-off');

      // prevent default link click
      e.preventDefault();

      // Trigger the form's submit button
      $(item).parents('form').find("input:button").click();

      // Perform Save Operation, if one set
      starRating.onSave();

      // Blur focus so window swapping doesn't cause odd behavior
      $(this).blur();

    }).hover(function() {
      // Handle star mouse over
      $(this).prevAll().andSelf().addClass('rating-over');
      $(this).nextAll().addClass('rating-off');
    }, function() {
      // Handle star mouse out
      $(this).siblings().andSelf().removeClass('rating-over');
      $(this).siblings().andSelf().removeClass('rating-off');
    }).focus(function() {
      // Handle keyboard focus
      $(this).prevAll().andSelf().addClass('rating-over');
      $(this).nextAll().addClass('rating-off');
    }).blur(function() {
      // Handle keyboard blur
      $(this).siblings().andSelf().removeClass('rating-over');
      $(this).siblings().andSelf().removeClass('rating-off');
    });
  },

  /*
   * Set a function to be executed when the user clicks a star.  This is done
   * immediately after the <input type="button" .../> is clicked.
   */
  setOnSave: function(fn) {
	  if (fn instanceof Function) {
		  starRating.onSave = fn;
	  }
  }
};