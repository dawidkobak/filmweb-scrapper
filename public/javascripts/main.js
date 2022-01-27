$(document).ready(function(){
	$('#plusIcon').click(function(){
	    $('#anotherPhrases').append(String.raw`<input type="text" placeholder="fraza" onfocus="this.placeholder=''" onblur="this.placeholder='fraza'" name="phrase">`);
	});

	$('#submitButton').click(function() {
        $.ajax({
        url: "http://localhost:9000/filmwebScrapper",
        type: "POST",
        data: $('#scrapForm').serializeArray(),
        success: function(result) {
            $("#result").html(result);
        },
        error: function(xhr,status,error) {
            $("#result").html(error);
        }
        });
        $('#scrapForm').reset();
    })
});


