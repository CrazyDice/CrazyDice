gameCrazyDice =  {
	init: function(wsUrl) {
		var ws = window['MozWebSocket'] ? MozWebSocket : WebSocket;
		this.wsUrl = wsUrl;
		this.playerSocket = new ws(wsUrl);

		$("#talk").keypress(handleReturnKey);            
		this.playerSocket.onmessage = receiveEvent;
	},

	sendMessage: function() {
		var tmpWs = this.playerSocket;
		tmpWs.send(JSON.stringify(
			{text: $("#talk").val()}
		));
		$("#talk").val('');
	},

	receiveEvent: function(event) {
		var data = JSON.parse(event.data);
		var tmpWs = this.playerSocket;

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


