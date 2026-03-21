let socket = new SockJS('http://localhost:8081/chat');
let stompClient = Stomp.over(socket);

let user = localStorage.getItem("user");

stompClient.connect({}, function() {
    stompClient.subscribe('/topic/messages', function(msg) {
        showMessage(JSON.parse(msg.body));
    });
});

function sendMessage() {
    let text = document.getElementById("message").value;

    if (!stompClient) {
        alert("Not connected yet!");
        return;
    }

    stompClient.send("/app/send", {}, JSON.stringify({
        sender: user,
        content: text
    }));
}

function showMessage(message) {
    let chat = document.getElementById("chat-box");
    chat.innerHTML += "<p><b>" + message.sender + ":</b> " + message.content + "</p>";
}