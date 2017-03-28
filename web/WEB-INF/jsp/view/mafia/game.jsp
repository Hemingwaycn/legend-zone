<%--@elvariable id="action" type="java.lang.String"--%>
<%--@elvariable id="gameId" type="long"--%>
<%--@elvariable id="username" type="java.lang.String"--%>
<!DOCTYPE html>
<html>
<head>
    <link rel="shortcut icon" href="resource/image/logo.png">
    <title>LegendZone :: Mafia</title>
</head>
<body>
<audio src="./resource/music/The Godfather Waltz.mp3" autoplay></audio>
<%@ include file="/header.jsp" %>
<div class="ui main text container">
    <h2>Mafia</h2>
    <div class="ui warning message">
        <p>生存与欺诈,在满足角色目标的前提下,尽可能活下去.</p>
        <p>目前仅聊天功能可用.灰色的身份尚未加入,敬请期待.</p>
    </div>

        <div class="ui red button role"></div>
        <div class="ui popup">
                <div class="column">
                    <h4 class="ui header">教父</h4>
                    <p>犯罪组织的领袖</p>
                    <p>夜晚杀死一人</p>
                    <p>夜间无敌</p>
                    <p>免疫调查</p>
                </div>
        </div>
        <div class="ui red button role"></div>
        <div class="ui popup">
            <div class="column">
                <h4 class="ui header">党徒</h4>
                <p>犯罪组织的普通成员</p>
                <p>夜晚与其他黑手党合作杀人</p>
            </div>
        </div>

    <div class="ui button role"></div>
    <div class="ui popup">
        <div class="column">
            <h4 class="ui header">伪装者</h4>
            <p>一个为黑手党工作的艺术家</p>
            <p>杀死一人并伪装成那个人</p>
        </div>
    </div>

    <div class="ui button role"></div>
    <div class="ui popup">
        <div class="column">
            <h4 class="ui header">军火贩</h4>
            <p>非法贩卖枪支牟利的大鳄,其背后势力正是黑手党</p>
            <p>在夜间进行警戒,自动杀死那晚所有访问你的人</p>
        </div>
    </div>

    <div class="ui button role"></div>
    <div class="ui popup">
        <div class="column">
            <h4 class="ui header">欺骗者</h4>
            <p>精通心理学的博士,可怕的是他在为黑手党工作</p>
            <p>每晚藏身在别人的住所中,当晚受到的所有主动能力效果都转向你的目标身上</p>
        </div>
    </div>

    <div class="ui button role"></div>
    <div class="ui popup">
        <div class="column">
            <h4 class="ui header">陷害者</h4>
            <p>一个以诬陷他人为生的黑手党成员</p>
            <p>每晚使一人被调查后,显示为黑手党或者中立邪恶者中的其中之一</p>
        </div>
    </div>

    <div class="ui green button role"></div>
    <div class="ui popup">
        <div class="column">
            <h4 class="ui header">市民</h4>
            <p>一个相信真理与正义的普通人</p>
            <p>拥有可用一晚的防弹背心</p>
        </div>
    </div>

    <div class="ui green button role"></div>
    <div class="ui popup">
        <div class="column">
            <h4 class="ui header">警长</h4>
            <p>一个执法机构的成员,迫于谋杀的威胁而隐匿</p>
            <p>每晚侦查一人有无犯罪能力</p>
        </div>
    </div>

    <div class="ui button role"></div>
    <div class="ui popup">
        <div class="column">
            <h4 class="ui header">保镖</h4>
            <p>一个身手敏捷的陆军退伍上尉,为了捍卫正义不惜献出生命</p>
            <p>在夜晚保护某人.如果某个人攻击了你保护的人,除了被保护的人之外攻击者和保镖都会死亡.</p>
        </div>
    </div>

    <div class="ui button role"></div>
    <div class="ui popup">
        <div class="column">
            <h4 class="ui header">舞娘</h4>
            <p>有名的交际花,用自己的方式来守卫着这座城市</p>
            <p>在夜晚限制某人,取消他的夜间能力.</p>
        </div>
    </div>

    <div class="ui button role"></div>
    <div class="ui popup">
        <div class="column">
            <h4 class="ui header">医生</h4>
            <p>一个拥有高超医术的医生,最大的志愿就是用自己能力保护三黑市民的安全</p>
            <p>救治一名玩家,使其免受一次死亡.</p>
        </div>
    </div>

    <div class="ui button role"></div>
    <div class="ui popup">
        <div class="column">
            <h4 class="ui header">侦探</h4>
            <p>一个私家侦探,跟踪能手</p>
            <p>侦查一个角色得知他当晚的目标.</p>
        </div>
    </div>




    <div id="status">&nbsp;</div>



    <div class="ui warning message">
        <div class="ui two column grid">
            <div class="twelve wide column" style=' height: 256px; overflow: auto; '>
                <div id="game_msg"></div>
            </div>
            <div class="four wide column" id="playerlist">

            </div>
        </div>

        <div class="ui fluid action input">
            <input type="text" placeholder="" id="inputBox">
            <div class="ui blue button" onclick="sendMsg()">send</div>
        </div>

    </div>

    <div class="ui warning message">
        <p>游戏帮助:</p>
        <p> 输入-start开始游戏(仅第一次输入有效)</p>
        <p> 输入-v x访问编号为x的玩家,即对其使用技能(仅夜间有效)</p>
    </div>



</div>
<script type="text/javascript" language="javascript">
    var move, sendMsg;
    $(document).ready(function () {

        var opponent = $("#opponent");
        var status = $("#status");
        var opponentUsername;
        var username = '<c:out value="${username}" />';
        var myTurn = false;

        $('.game-cell').addClass('span1');

        if (!("WebSocket" in window)) {
            // todo
            return;
        }


        var server;
        try {
            server = new WebSocket('ws://' + window.location.host +
                    '<c:url value="/mafia/${gameId}/${username}"><c:param name="action" value="${action}" /></c:url>');
        } catch (error) {
            //todo
            return;
        }

        server.onopen = function (event) {
            //todo
        };

        window.onbeforeunload = function () {
            server.close();
        };

        server.onclose = function (event) {
            if (!event.wasClean || event.code != 1000) {
                toggleTurn(false, 'Game over due to error!');
            }
        };

        server.onerror = function (event) {
            //todo
        };

        server.onmessage = function (event) {
            var message = JSON.parse(event.data);
            if (message.action == 'gameStarted') {

            } else if (message.action == 'opponentMadeMove') {

            } else if (message.action == 'gameOver') {
                toggleTurn(false, 'Game Over!');

            } else if (message.action == 'gameIsDraw') {
                toggleTurn(false, 'The game is a draw. ' +
                        'There is no winner.');
            } else if (message.action == 'gameForfeited') {
                toggleTurn(false, 'Your opponent forfeited!');

            } else if (message.action == 'text') {
                $("#game_msg").html($("#game_msg").html()  + message.msg + "</br>");
            } else if (message.action == 'playerJoin'){
                $("#game_msg").html($("#game_msg").html()  + message.msg + " 进入了城镇.</br>");
                $("#playerlist").html($("#playerlist").html() + "<div class='ui white fluid button'>" + message.msg + "</div>");
            } else if (message.action == 'playerExit'){
                $("#game_msg").html($("#game_msg").html()  + message.msg + " 离开了.</br>");
                $("#playerlist").children().each(function(i,n){
                    var obj = $(n);
                    if(obj.html() == message.msg){
                        $("#playerlist").find("div")[i].remove();
                        return;
                    }
                });
            }else if (message.action == 'assignRole') {
                $("#game_msg").html($("#game_msg").html()  + "你的身份是 " + message.msg + ".</br>");
            }
        };


        //发送消息
        sendMsg = function () {
            var msg = $('#inputBox').val();
            if (msg != "") {
                server.send(JSON.stringify({msg: msg}));
                $('#inputBox').val("");
            }
        };
        $(function(){
            $('#inputBox').bind('keypress',function(event){
                if(event.keyCode == "13") {
                    sendMsg();
                }
            });
        });


        $('.button.role').popup({});

    });
</script>

<%@ include file="/footer.jsp" %>

</body>

</html>
