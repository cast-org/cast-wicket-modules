var myObject;
var editableGrid; 
var grids = {} ;
var myObjects = {};

function cwmImportGrid(divId, url, readonly) {
	grids[divId] = new EditableGrid(divId);
	editableGrid = grids[divId];

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

	//pull text down from URL
	var data = $.ajax({type: "GET", url: url, async: false}).responseText;

	//parse string into object
	myObjects[divId] = JSON.parse(data);
}

function initializeGrid(divId) {
	this.editableGrid = grids[divId];

	with (this.editableGrid) {
		modelChanged = function(rowIndex, columnIndex, oldValue, newValue, row) {
			myObject = myObjects[divId];
			myObject.data[rowIndex].values[myObject.metadata[columnIndex].name] = newValue;			
		};
	}
}

function cwmExportGrid(textAreaId, divId) {
	myObject = myObjects[divId];
	jQuery('#'+textAreaId).val(JSON.stringify(myObject));  	
}

function cwmAddRow(divId) {	
	console.log("calling addrow");
	editableGrid = grids[divId];
	myObject = myObjects[divId];
	var objectValue = {"id":myObject.data.length+1, "values":{"c1":"","c2":"","c3":"","c4":"","c5":""}};
	myObject.data.push(objectValue);
	editableGrid.append("Row" + myObject.data.length, objectValue, objectValue, true);
	synchronizeDataToMedataDimension(divId);
	grids[divId] = editableGrid;
	myObjects[divId] = myObject;	
}

function cwmRemoveRow(divId) {
	editableGrid = grids[divId];
	myObject = myObjects[divId];
	editableGrid.remove(myObject.data.length - 1);
	myObject.data.splice(myObject.data.length - 1,1);
	grids[divId] = editableGrid;
	myObjects[divId] = myObject;	
	synchronizeDataToMedataDimension(divId);
}

function cwmAddColumn(divId) {
	editableGrid = grids[divId];
	myObject = myObjects[divId];
	var nextColumnNumber = myObject.metadata.length + 1;
	var nextColumnLabel = "Column " + nextColumnNumber;
	var nextColumnName = "c" + nextColumnNumber;
	var newColumn = {"name":nextColumnName,"label":nextColumnLabel,"datatype":"string","editable":true};
	myObject.metadata.push(newColumn);
	editableGrid.addColumn();
	grids[divId] = editableGrid;
	myObjects[divId] = myObject;	
	synchronizeDataToMedataDimension(divId);
}

function cwmRemoveColumn(divId) {	
	editableGrid = grids[divId];
	myObject = myObjects[divId];
	editableGrid.deleteColumn();
	myObject.metadata.splice(myObject.metadata.length - 1,1);
	grids[divId] = editableGrid;
	myObjects[divId] = myObject;	
	synchronizeDataToMedataDimension(divId);
}

function generateColumnNameByIndex(indexOfColumn) {
	var ColumnName = "c" + indexOfColumn;
	return ColumnName;
}

function synchronizeDataToMedataDimension(divId) {
	editableGrid = grids[divId];
	myObject = myObjects[divId];
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
	grids[divId] = editableGrid;
	myObjects[divId] = myObject;	
}