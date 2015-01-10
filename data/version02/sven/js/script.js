(function ($){

function init () {
	$('nav .menu-title').click(function(){
		$('#wrapper').toggleClass('active-menu');
	});
	$("pre.code.html").snippet("html",{style:"dull",menu:false,showNum:true});
	$("pre.code.java").snippet("html",{style:"dull",menu:false,showNum:true});
	$("pre.code.php").snippet("php",{style:"dull",menu:false,showNum:true});
}

$(document).ready(init);
})(jQuery);
