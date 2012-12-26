//
// This code editablegrid_cast is the customized code used to implement editablegrid.
// EditableGrid is an open source javascript package used to create castEditableGrids and tables.
// More information on editable grid can be found at http://http://www.editablegrid.net/
// Original Source code can be found https://github.com/webismymind/editablegrid/downloads
// 
// EditableGrid is currently used within the response areas.  When creating a new grid
// a json string is sent via URL with the initial grid values.  When saving a grid
// the values in the grid are sent as a json string to a textarea to be passed back 
// to the database
//
var castEditableGrids = {};  		// the grid that you view
var castEditableGridObjects = {};  	// the data representation of the grid

//
// Setup the grid:  The divId is the id of the <div> for the grid, it must be unique on the page.
// The url is where the initial grid state is found as a json string.
//
function cwmImportGrid(divId, url, readonly) {
	
	castEditableGrids[divId] = new EditableGrid(divId);
	var editableGrid = castEditableGrids[divId];
	
	editableGrid.tableLoaded = function() { 
		if(readonly=="true") {
			for ( i=0; i < editableGrid.getColumnCount(); i++) {
				editableGrid.getColumn(i).editable = false;	
			}			
		}
		this.renderGrid(divId, "dataTableResponse"); 
	};
	
	editableGrid.loadJSON(url);
	this.initializeGrid(divId);

	//pull json string from the URL
	var data = $.ajax({type: "GET", url: url, async: false}).responseText;
	
	//parse string into object
	castEditableGridObjects[divId] = JSON.parse(data);
	
	setRemoveButtonState(divId);

}

function initializeGrid(divId) {
	var editableGrid = castEditableGrids[divId];

	editableGrid.modelChanged = function(rowIndex, columnIndex, oldValue, newValue, row) {
			var myObject = castEditableGridObjects[divId];
			myObject.data[rowIndex].values[myObject.metadata[columnIndex].name] = newValue;
			castEditableGridObjects[divId] = myObject;
	};		
}

function setRemoveButtonState(divId) {	
	var myObject = castEditableGridObjects[divId];

	//Disable delete button when there is only one row or column
	if (myObject.data.length == 1) {
		jQuery('#'+divId).parent().find('.delRow').addClass('off');
	}
	if (myObject.metadata.length == 1) {
		jQuery('#'+divId).parent().find('.delColumn').addClass('off');
	} 
}


// export the json value of the grid to the textarea
function cwmExportGrid(textAreaId, divId) {
	var myObject = castEditableGridObjects[divId];	
	jQuery('#'+textAreaId).val(JSON.stringify(myObject));  	
}

function cwmAddRow(divId) {	
	var editableGrid = castEditableGrids[divId];
	var myObject = castEditableGridObjects[divId];
	var objectValue = {"id":myObject.data.length+1, "values":{"c1":"","c2":"","c3":"","c4":"","c5":""}};

	myObject.data.push(objectValue);
	editableGrid.append("Row" + myObject.data.length, objectValue, objectValue, true);
	synchronizeDataToMedataDimension(divId);
	castEditableGrids[divId] = editableGrid;
	castEditableGridObjects[divId] = myObject;	

	//Enable delete button when there is more than one row
	if (myObject.data.length > 1) {
		jQuery('#'+divId).parent().find('.delRow').removeClass('off');
	}
}

function cwmRemoveRow(divId) {	
	var editableGrid = castEditableGrids[divId];
	var myObject = castEditableGridObjects[divId];

	//prevent removing the last 
	if ((myObject.data.length) > 1) {
		editableGrid.remove(myObject.data.length - 1);
		myObject.data.splice(myObject.data.length - 1,1);
		castEditableGrids[divId] = editableGrid;
		castEditableGridObjects[divId] = myObject;
		synchronizeDataToMedataDimension(divId);
	}
	//Disable button - set class "off"
	if (myObject.data.length == 1) {
		jQuery('#'+divId).parent().find('.delRow').addClass('off');
	}
}

function cwmAddColumn(divId) {	
	var editableGrid = castEditableGrids[divId];
	var myObject = castEditableGridObjects[divId];
	
	var nextColumnNumber = myObject.metadata.length + 1;
	var nextColumnLabel = "Column " + nextColumnNumber;
	var nextColumnName = "c" + nextColumnNumber;
	
	// LDM - this needs to take in the actual column type and the editable setting
	var newColumn = {"name":nextColumnName,"label":nextColumnLabel,"datatype":"string","editable":true};
	
	myObject.metadata.push(newColumn);
	editableGrid.addColumn();
	castEditableGrids[divId] = editableGrid;
	castEditableGridObjects[divId] = myObject;	
	synchronizeDataToMedataDimension(divId);

	//Enable delete button when there is more than one column
	if (myObject.metadata.length > 1) {
		jQuery('#'+divId).parent().find('.delColumn').removeClass('off');
	}
}

function cwmRemoveColumn(divId) {	
	var editableGrid = castEditableGrids[divId];
	var myObject = castEditableGridObjects[divId];
	
	//prevent removing the last 
	if ((myObject.metadata.length) > 1) {
		editableGrid.deleteColumn();
		myObject.metadata.splice(myObject.metadata.length - 1,1);
		castEditableGrids[divId] = editableGrid;
		castEditableGridObjects[divId] = myObject;	
		synchronizeDataToMedataDimension(divId);
	}

	//Disable button - set class "off"
	if (myObject.metadata.length == 1) {
		jQuery('#'+divId).parent().find('.delColumn').addClass('off');
	}
}

function generateColumnNameByIndex(indexOfColumn) {
	var ColumnName = "c" + indexOfColumn;
	return ColumnName;
}

function synchronizeDataToMedataDimension(divId) {
	var myObject = castEditableGridObjects[divId];
	
	for (i=0; i<myObject.data.length; i++) {
		
		//remove excess data cells
		for (k=0; k < Object.keys(myObject.data[i].values).length - myObject.metadata.length; k++) {
			delete myObject.data[i].values[generateColumnNameByIndex(k+1+myObject.metadata.length)];
		}
		//add missing data entries with empty string as default value
		for (j=0; j < myObject.metadata.length; j++) {				
			if (typeof myObject.data[i].values[generateColumnNameByIndex(j+1)] == 'undefined') {
				myObject.data[i].values[generateColumnNameByIndex(j+1)] = "";
			}
		}
	}	
	castEditableGridObjects[divId] = myObject;	
}