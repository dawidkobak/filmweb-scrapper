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
            let newHtml = '<h3>Dla podanych fraz zescrapowano i zapisano w ElasticSearch następującą liczbę rekordów:</h3>';
            let jsArray = JSON.parse(result);
            for (let i = 0; i < jsArray[0].length; i++) {
                newHtml += '<p>' + jsArray[0][i].phrase + ' -> ' + jsArray[0][i].count + '</p>';
            }
            $("#result").html(newHtml);
        },
        error: function(xhr,status,error) {
            $("#result").html(error);
        }
        });
    })
});


