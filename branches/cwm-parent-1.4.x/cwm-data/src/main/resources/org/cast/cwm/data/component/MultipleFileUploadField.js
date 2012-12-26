/**
 * Convert a single file-input element into a 'multiple' input list
 *
 * Usage:
 *
 *   1. Create a file input element (no name)
 *      eg. <input type="file" id="first_file_element">
 *
 *   2. Create a DIV for the output to be written to
 *      eg. <div id="files_list"></div>
 *
 *   3. Instantiate a MultiSelector object, passing in the DIV and an (optional) maximum number of files
 *      eg. var multi_selector = new MultiSelector( document.getElementById( 'files_list' ), 3 );
 *
 *   4. Add the first element
 *      eg. multi_selector.addElement( document.getElementById( 'first_file_element' ) );
 *
 *   5. That's it.
 *
 *   You might (will) want to play around with the addListRow() method to make the output prettier.
 *
 *   You might also want to change the line 
 *       element.name = 'file_' + this.count;
 *   ...to a naming convention that makes more sense to you.
 * 
 * Licence:
 *   Use this however/wherever you like, just don't blame me if it breaks anything.
 *
 * Credit:
 *   If you're nice, you'll leave this bit:
 *  
 *   Class by Stickman -- http://www.the-stickman.com
 *      with thanks to:
 *      [for Safari fixes]
 *         Luis Torrefranca -- http://www.law.pitt.edu
 *         and
 *         Shawn Parker & John Pennypacker -- http://www.fuzzycoconut.com
 *      [for duplicate name bug]
 *         'neal'
 */
function MultiSelector( eprefix, list_target, max){

	// Where to write the list
	this.list_target = list_target;
	// How many elements?
	this.count = 0;
	// How many elements?
	this.id = 0;
	// Is there a maximum?
	if( max ){
		this.max = max;
	} else {
		this.max = -1;
	};
	
	this.element_name_prefix=eprefix;
	
	this.delete_element_label="Delete";
	this.delete_element_type="input";
	this.delete_element_class="mf_delete";
	this.input_element_title="Upload Field";
	
	/**
	 * Add a new file input element
	 */
	this.addElement = function( element ){

		// Make sure it's a file input element
		if( element.tagName.toLowerCase() == 'input' && element.type.toLowerCase() == 'file' ){

			// Element name -- what number am I?
			element.name = this.element_name_prefix + "_mf_"+this.id++;

			// Add reference to this object
			element.multi_selector = this;

			// What to do when a file is selected
			element.onchange = function(){

				// New file input
				var new_element = document.createElement( 'input' );
				new_element.type = 'file';
				new_element.title = this.multi_selector.input_element_title;

				// Add new element
				this.parentNode.insertBefore( new_element, this );

				// Apply 'update' to element
				this.multi_selector.addElement( new_element );

				// Update list
				this.multi_selector.addListRow( this );

				// Hide this: we can't use display:none because Safari doesn't like it
				this.style.position = 'absolute';
				this.style.left = '-1000px';

			};
			// If we've reached maximum number, disable input element
			if( this.max != -1 && this.count >= this.max ){
				element.disabled = true;
			};

			// File element counter
			this.count++;
			// Most recent element
			this.current_element = element;
			
		} else {
			// This can only be applied to file input elements!
			alert( 'Error: not a file input element' );
		};

	};

	/**
	 * Add a new row to the list of files
	 */
	this.addListRow = function( element ){

		// Row div
		var new_row = document.createElement( 'div' );

		// Delete button
		var delete_button = document.createElement(this.delete_element_type);
		if (delete_button.tagName.toLowerCase() == "input") {
			delete_button.type = 'button';
			delete_button.value = this.delete_element_label;
		} else if (delete_button.tagName.toLowerCase() == "a") { 
			delete_button.href = 'javascript:void(0);';
			delete_button.innerHTML = this.delete_element_label;
		}
		delete_button.setAttribute("class", this.delete_element_class);

		// References
		new_row.element = element;

		// Delete function
		delete_button.onclick= function(){

			// Remove element from form
			this.parentNode.element.parentNode.removeChild( this.parentNode.element );

			// Remove this row from the list
			this.parentNode.parentNode.removeChild( this.parentNode );

			// Decrement counter
			this.parentNode.element.multi_selector.count--;

			// Re-enable input element (if it's disabled)
			this.parentNode.element.multi_selector.current_element.disabled = false;

			// Appease Safari
			//    without it Safari wants to reload the browser window
			//    which nixes your already queued uploads
			return false;
		};

		// Remove lame IE Prefix
		var path = element.value;
		var lameIePrefix = "C:\\fakepath\\";
		if(path.length > lameIePrefix.length 
				&& path.substr(0, lameIePrefix.length) === lameIePrefix) {
			path = path.substr(lameIePrefix.length);
		}
		
		// Set row value
		new_row.innerHTML = "<span class=\"wicket-mfu-filename\">" + path + "</span>";
		
		// Add button
		new_row.appendChild( delete_button );

		// Add it to the list
		this.list_target.appendChild( new_row );
		
	};
	
	this.setDeleteElementType = function(type) {
		if ((typeof type == "string") && (type == "input" || type == "a")) {
			this.delete_element_type = type;
		}
	}
	
	this.setDeleteElementClass = function(clazz) {
		if (typeof clazz == "string") {
			this.delete_element_class = clazz;
		}
	}
	
	this.setDeleteElementLabel = function(label) {
		if (typeof label == "string") {
			this.delete_element_label = label;
		}
	}
	
	this.setInputElementTitle = function(title) {
		if (typeof title == "string") {
			this.input_element_title = label;
		}
	}
};