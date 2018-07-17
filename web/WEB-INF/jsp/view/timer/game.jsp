<%--@elvariable id="action" type="java.lang.String"--%>
<%--@elvariable id="gameId" type="long"--%>
<%--@elvariable id="username" type="java.lang.String"--%>
<!DOCTYPE html>
<html>
<head>
    <link rel="shortcut icon" href="resource/image/logo.png">
    <title>CubeTimer</title>
    <link rel="stylesheet" href="<c:url value="/resource/stylesheet/timer.css" />"/>

    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no" >

    <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
    <link rel="stylesheet" type="text/css" href="./resource/semantic.min.css">
    <script src="./resource/semantic.min.js"></script>
</head>
<body>

<div class="ui main text container" id="touch">
    <div class="two ui buttons">
        <button class="ui green button"><span class="player-label"></span> ${username}</button>
        <div class="or" data-text="vs"></div>
        <button class="ui red loading button"><span class="player-label"></span><span id="opponent"></span></button>
    </div>
    <div class="ui warning message" id="scramble">支持手机和电脑计时
        </br>
        打乱功能测试中,双方将看到相同打乱,暂时不是WCA打乱...
    </div>


    <script src="resource/js/timer.js"></script>
    <div id="time">
        <button class="fluid massive ui blue button" id="mytime" style="font-size:48px;">
            00:00:00
        </button>
    </div>

    <div class="ui" id = "game_msg">
        <table class="ui orange table" id = "result">
            <thead id="score">
            <tr><th>score (mean)</th>
                <th></th>
                <th></th>
            </tr>
            </thead>
            <tbody id = 'result_table'>

            </tbody>
        </table>
    </div>
</div>



<script type="text/javascript" language="javascript">

    //补零
    function toDub(n) {
        return n < 10 ? "0" + n : "" + n;
    }

    function str2float(str){
        var ms = str.split(":")
        var value = parseFloat(ms[0])*60 + parseFloat(ms[1]);
        return value;
    }

    function float2str(n){
        var m = parseInt(n / 60000)
        var s = parseInt((n / 1000) % 60);
        var M = parseInt((n % 1000) / 10);
        return toDub(m) + ":" + toDub(s) + "." + toDub(M);
    }
</script>
<script type="text/javascript" language="javascript">
    var move;
    $(document).ready(function () {
        var opponent = $("#opponent");
        var game_msg = $("#game_msg");
        var status = $("#scramble");
        var opponentUsername;
        var username = '<c:out value="${username}" />';
        var myTurn = false;
        var round_num = 0;
        var win0 = 0;
        var win1 = 0;
        var sum0 = 0.0;
        var sum1 = 0.0;

        // 计时控制
        var start = false;
        var timer = null;
        var n = 0;
        var startTime = 0;

        var oneSec = 0;
        var oneSecTimer = null;
        game_msg.height(document.body.clientHeight - 300 - 16);

        //$("#mytime")[0].style.color = '#fff';



        $("#mytime")[0].ontouchstart = function () {
            if (!myTurn) {
            } else {
                //开始计时
                if (start) {
                } else {
                    start = true;
                    $("#mytime")[0].style.color = '#f00';
                    oneSec = new Date().getTime();
                    oneSecTimer = setInterval(function () {
                        if (new Date().getTime() - oneSec >= 500) {
                            $("#mytime")[0].style.color = '#0f0';
                        }
                    }, 10);
                }
            }
            return false;
        }

        $("#touch")[0].ontouchstart = function () {
            if (!myTurn) {
                // showModal('请等一下你的对手');
            } else {
                //开始计时
                if (start) {
                    startTime = new Date().getTime();
                    if (startTime - oneSec < 500) {
                        return;
                    }
                    start = false;
                    clearInterval(timer);
                    n = 0;
                    startTime = new Date().getTime();
                    move($("#mytime").html());
                } else {

                }
            }
//                    return false;
        }

        $("#mytime")[0].ontouchend = function () {
            clearInterval(oneSecTimer);
            //开始计时
            $("#mytime")[0].style.color = '#fff';
            if (start) {
                startTime = new Date().getTime();
                if (startTime - oneSec < 500) {
                    start = false;
                    return;
                }
                clearInterval(timer);
                timer = setInterval(function () {
                    currTime = new Date().getTime();
                    n = currTime - startTime;
                    $("#mytime").html(float2str(n));
                }, 10);
            }
            return false;
        }


        var kflag = false;
        window.onkeydown = function(event){
            if(event.keyCode == 32){
                if( kflag ){
                    return;
                }else{
                    kflag = true;
                }
                if(start){
                    $("#touch")[0].ontouchstart();
                }else{
                    $("#mytime")[0].ontouchstart();
                }
            }
        };

        window.onkeyup = function(event){
            if(event.keyCode == 32){
                kflag = false;
                $("#mytime")[0].ontouchend();
            }
        };

        if (!("WebSocket" in window)) {
            showModal('WebSockets are not supported.');
            return;
        }

        var server;
        try {
            server = new WebSocket('ws://' + window.location.host + '<c:url value="/timer/${gameId}/${username}"><c:param name="action" value="${action}" /></c:url>');
        } catch (error) {
            showModal(error);
            return;
        }

        server.onopen = function (event) {
        };

        window.onbeforeunload = function () {
            server.close();
        };

        server.onclose = function (event) {
            if (!event.wasClean || event.code != 1000) {
                toggleTurn(false, '与服务端失去连接.');
                showModal('Code ' + event.code + ': ' +
                        event.reason);
            }
        };

        server.onerror = function (event) {
            showModal(event.data);
        };

        server.onmessage = function (event) {
            var message = JSON.parse(event.data);
            if (message.action == 'gameStarted') {
                if (message.game.player1 == username)
                    opponentUsername = message.game.player2;
                else
                    opponentUsername = message.game.player1;
                opponent.text(opponentUsername);
                $(".loading").removeClass("loading");
                ;
                //toggleTurn(message.game.nextMoveBy == username);
                toggleTurn(true);
                $("#scramble").html(message.scramble);

            } else if (message.action == 'opponentMadeMove') {
                $('#r' + message.move.row + 'c' + message.move.column)
                 .unbind('click')

                toggleTurn(true);
                $("#scramble").html(message.scramble);
            } else if (message.action == 'gameOver') {
                toggleTurn(false, 'Game Over!');
                if (message.winner) {
                    showModal('Congratulations, you won!');
                } else {
                    showModal('User "' + opponentUsername +
                            '" won the game.');
                }
            } else if (message.action == 'gameIsDraw') {
                toggleTurn(false, 'The game is a draw. ' +
                        'There is no winner.');
                showModal('The game ended in a draw. ' +
                        'Nobody wins!');
            } else if (message.action == 'gameForfeited') {
                toggleTurn(false, '对手失去连接.');
//                        showModal('User "' + opponentUsername + '" forfeited the game. You win!');
                showModal('对手失去连接.');
            } else if (message.action == 'text') {
                $("#game_msg").html("<div class='ui info message'>" + message.msg + "</div>" + $("#game_msg").html());
            } else if (message.action == 'result'){
                round_num++;
                // calc sum
                sum0 += str2float(message.r0);
                sum1 += str2float(message.r1);

                //
                var template = "<tr> <td>round @x</td> <td class=\"@w0\">@0</td> <td class=\"@w1\">@1</td> </tr>";
                var tmp = template;
                tmp = tmp.replace("@x",round_num);
                tmp = tmp.replace("@0",message.r0);
                tmp = tmp.replace("@1",message.r1);
                if(message.r0<message.r1){
                    tmp = tmp.replace("@w0","positive");
                }
                if(message.r1<message.r0){
                    tmp = tmp.replace("@w1","positive");
                }
                $("#result_table").html(tmp + $("#result_table").html());

                template = "<tr><th>score (mean)</th> <th>@0 (@m0)</th> <th>@1 (@m1)</th> </tr>";
                tmp = template;
                if(message.r0<message.r1)win0++;
                if(message.r1<message.r0)win1++;
                tmp = tmp.replace("@0",win0);
                tmp = tmp.replace("@1",win1);
                tmp = tmp.replace("@m0",float2str(sum0/round_num*1000));
                tmp = tmp.replace("@m1",float2str(sum1/round_num*1000));
                $("#score").html(tmp);
            }
        };

        var toggleTurn = function (isMyTurn, message) {
            myTurn = isMyTurn;
            if (myTurn) {
                //status.text(message || '你可以开始还原了.');
            } else {
                status.text(message || '等待对手.');
            }
        };

        move = function (time) {
            if (!myTurn) {
                showModal('It is not your turn yet!');
                return;
            }
            if (server != null) {
                server.send(JSON.stringify({time: time}));
                toggleTurn(false);
            } else {
                showModal('Not connected to came server.');
            }
        };
    });
</script>

</body>
</html>
