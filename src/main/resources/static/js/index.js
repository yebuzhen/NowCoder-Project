$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// Put CSRF token setting into the header of the message before sending AJAX request
	// var token = $("meta[name='_csrf']").attr("content");
	// var header = $("meta[name='_csrf_header']").attr("content");
	// $(document).ajaxSend(function (e, xhr, options) {
	// 	xhr.setRequestHeader(header, token);
	// })

	//Get the title and content
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	//Send synchronized post
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title": title, "content": content},
		function (data) {

			data = $.parseJSON(data);

			//Show callback message in the notification box
			$("#hintBody").text(data.msg);

			//Show the notification box
			$("#hintModal").modal("show");

			//Hide after 2 seconds
			setTimeout(function(){
				$("#hintModal").modal("hide");

				//Refresh the page
				if (data.code == 0) {
					window.location.reload();
				}

			}, 2000);

		}
	)


}