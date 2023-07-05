//로그인 시스템 대신 임시
let username = prompt("아이디를 입력하세요");
let roomNum = prompt("채팅방 번호를 입력하세요");

document.querySelector("#username").innerHTML = username;

//SSE연결하기
const eventSource = new EventSource(
  `http://localhost:8080/chat/roomNum/${roomNum}`
);

eventSource.onmessage = (event) => {
  const data = JSON.parse(event.data);
  if (data.sender === username) {
    //로그인한 유저가 보낸 메시지
    //파란박스(오른쪽)
    initMyMessage(data);
  } else {
    //회색박스(왼쪽)
    initYourMessage(data);
  }
};

//파란박스 만들기
function getSendMsgBox(data) {
  let md = data.createdAt.substring(5, 10);
  let tm = data.createdAt.substring(11, 16);
  converTime = tm + " | " + md;

  return `<div class="sent_msg">
    <p>${data.msg}</p>
    <span class="time_date"> ${converTime} / <b>${data.sender}</b> </span>
  </div>`;
}

//회색박스 만들기
function getReceiveMsgBox(data) {
  let md = data.createdAt.substring(5, 10);
  let tm = data.createdAt.substring(11, 16);
  converTime = tm + " | " + md;

  return `<div class="received_withd_msg">
    <p>${data.msg}</p>
    <span class="time_date"> ${converTime} / <b>${data.sender}</b> </span>
  </div>`;
}

//최초 초기화 될 때 1번방에 3건이 있으면 3건을 다 가져옴
//addMessage함수가 호출되면 db에 insert되고 그 데이터가 자동으로 흘러들어와(SEE) 그럼 initMyMessage 함수가 실행되어 append됨.
//파란박스 초기화하기
function initMyMessage(data) {
  let chatBox = document.querySelector("#chat-box");

  let sendBox = document.createElement("div");
  sendBox.className = "outgoing_msg";
  sendBox.innerHTML = getSendMsgBox(data);
  chatBox.append(sendBox);
  //채팅 메시지 보낼때 받을 때 자동 스크롤 되게 설정
  document.documentElement.scrollTop = document.body.scrollHeight;
}

//회색박스 초기화 하기
function initYourMessage(data) {
  let chatBox = document.querySelector("#chat-box");

  let receivedBox = document.createElement("div");
  receivedBox.className = "received_msg";
  receivedBox.innerHTML = getReceiveMsgBox(data);
  chatBox.append(receivedBox);
  //채팅 메시지 보낼때 받을 때 자동 스크롤 되게 설정
  document.documentElement.scrollTop = document.body.scrollHeight;
}

//AJAX채팅 메시지 전송(RoomNum으로 보냄)
async function addMessage() {
  let msgInput = document.querySelector("#chat-outgoing-msg");

  let chat = {
    sender: username,
    roomNum: roomNum,
    msg: msgInput.value,
  };

  //fetch통신시 시간이 걸려서 null을 먼저 출력하고 통신함. 해서 await로 기다려줘야함. 또한 await로 인해 다른 모든 함수도 실행되지 않아서 비동기 함수로 수정해줘야함.
  await fetch("http://localhost:8080/chat", {
    method: "post", //http post 메서드 (새로운 데이터를 write할때 사용)
    body: JSON.stringify(chat), //JS -> JSON으로 변경
    headers: {
      //내가 보내는 데이터 타입을 알려줌
      "Content-Type": "application/json;charset=utf-8",
    },
  });

  msgInput.value = "";
}

//버튼 클릭시 메시지 전송
document.querySelector("#chat-send").addEventListener("click", () => {
  //   alert("클릭됨");
  addMessage();
});

//엔터를 치면 메시지 전송
document
  .querySelector("#chat-outgoing-msg")
  .addEventListener("keydown", (e) => {
    console.log(e.keyCode);
    if (e.keyCode === 13) {
      //   alert("클릭됨");
      addMessage();
    }
  });
