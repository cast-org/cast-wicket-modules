function skipLink() {
    var is_webkit = navigator.userAgent.toLowerCase().indexOf('webkit') > -1;
    var is_opera = navigator.userAgent.toLowerCase().indexOf('opera') > -1;
    
    if(is_webkit || is_opera) {
    	var target = document.getElementById('skiptarget');
    	target.href="#skiptarget";
    	target.innerText="Start of main content";
    	target.setAttribute("tabindex" , "0");
    	document.getElementById('skiplink').setAttribute("onclick" , "document.getElementById('skiptarget').focus();");
    }
}

$(window).ready(function() {
    skipLink();
}