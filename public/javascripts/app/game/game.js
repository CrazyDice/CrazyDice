game = game || (function() {
	var _wsUrl = "";
	var _playerSocket = null;
	return {
		init: function(wsUrl) {
			var socketType = window['MozWebSocket'] ? MozWebSocket : WebSocket;
			_wsUrl = wsUrl;
			_playerSocket = new socketType(_wsUrl);
	
			$("#talk").keypress(handleReturnKey);            
			_playerSocket.onmessage = receiveEvent;
		},
	
		sendMessage: function() {
			var tmpWs = _playerSocket;
			tmpWs.send(JSON.stringify(
				{text: $("#talk").val()}
			));
			$("#talk").val('');
		},
	
		receiveEvent: function(event) {
			var data = JSON.parse(event.data);
			var tmpWs = _playerSocket;
	
			// Handle errors
			if(data.error) {
				tmpWs.close();
				$("#onError span").text(data.error);
				$("#onError").show();
				return;
			} else {
				$("#onChat").show();
			}
	
			// Create the message element
			var el = $('<div class="message"><span></span><p></p></div>');
			$("span", el).text(data.user);
			$("p", el).text(data.message);
			$(el).addClass(data.kind);
			if(data.user == '@userId') {
				$(el).addClass('me');
			}
			$('#messages').append(el);
	
			// Update the members list
			$("#members").html('');
			$(data.members).each(function() {
				$("#members").append('<li>' + this + '</li>');
			});
		},
	
		handleReturnKey: function(e) {
			if(e.charCode == 13 || e.keyCode == 13) {
				e.preventDefault();
				sendMessage();
			}
		}
	};
})();