<%--@elvariable id="action" type="java.lang.String"--%>
<%--@elvariable id="gameId" type="long"--%>
<%--@elvariable id="username" type="java.lang.String"--%>
<!DOCTYPE html>
<html>
<head>
    <link rel="shortcut icon" href="resource/image/logo.png">
    <title>LegendZone :: Gomoku</title>
    <link rel="stylesheet" href="http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/css/bootstrap.min.css"/>
    <link rel="stylesheet"
          href="<c:url value="/resource/stylesheet/gomoku.css" />"/>
</head>
<body>
<%@ include file="/header.jsp" %>
<div class="ui main text container">
    <h2>Gomoku</h2>

    <div class="two ui buttons">
        <button class="ui green button"><span class="player-label"></span> ${username}</button>
        <div class="or" data-text="vs"></div>
        <button class="ui red loading button"><span class="player-label"></span><span id="opponent"></span></button>
    </div>

    <div class="ui warning message">游戏规则:黑子先行,黑白双方轮流落子.首先在横,竖,斜方向上成五(连续五个己方棋子)者为胜.</div>
    <div class="ui warning message"><div id="status">&nbsp;</div></div>
    <div id="gameContainer">
    </div>
    <div id="game_msg"></div>
</div>

<script type="text/javascript" language="javascript">

    function initBoard() {
        var board = $("#gameContainer");
        for (var i = 0; i < 15; i++) {
            var str = "";
            str += "<div class=\"row\">";
            for (var j = 0; j < 15; j++) {
                str += "<div id=\"r";
                str += i.toString();
                str += "c";
                str += j.toString();
                str += "\" class=\"game-cell\" onclick=\"move(";
                str += i.toString();
                str += ",";
                str += j.toString();
                str += ");\"></div>";
            }
            str += " </div>";
            board.append(str);
        }
    }
    ;

    var move;
    $(document).ready(function () {
        initBoard();
        var opponent = $("#opponent");
        var status = $("#status");
        var opponentUsername;
        var username = '<c:out value="${username}" />';
        var myTurn = false;

        $('.game-cell').addClass('span1');

        if (!("WebSocket" in window)) {
            showModal('WebSockets are not supported in this ' +
                    'browser. Try Internet Explorer 10 or the latest ' +
                    'versions of Mozilla Firefox or Google Chrome.');
            return;
        }


        var server;
        try {
            server = new WebSocket('ws://' + window.location.host +
                    '<c:url value="/gomoku/${gameId}/${username}">
            <c:param name="action" value="${action}" />
            </c:url>');
        } catch (error) {
            showModal(error);
            return;
        }

        server.onopen = function (event) {
            /*modalWaitingBody
                    .text('Waiting on your opponent to join the game.');
            modalWaiting.modal({keyboard: false, show: true});*/
        };

        window.onbeforeunload = function () {
            server.close();
        };

        server.onclose = function (event) {
            if (!event.wasClean || event.code != 1000) {
                toggleTurn(false, 'Game over due to error!');
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
                $(".loading").removeClass("loading");;
                //toggleTurn(message.game.nextMoveBy == username);
                toggleTurn(true);
            } else if (message.action == 'opponentMadeMove') {
                $('#r' + message.move.row + 'c' + message.move.column)
                        .unbind('click')
                        .removeClass('game-cell-selectable')
                        .addClass('game-cell-opponent game-cell-taken');
                toggleTurn(true);
            } else if (message.action == 'gameOver') {
                toggleTurn(false, 'Game Over!');
                if (message.winner) {
                    showModal("Congratulations, you won!");
                } else {
                    showModal('传说大魔王是不可战胜的!');
                }
            } else if (message.action == 'gameIsDraw') {
                toggleTurn(false, 'The game is a draw. ' +
                        'There is no winner.');
                showModal('The game ended in a draw. ' + 'Nobody wins!');
            } else if (message.action == 'gameForfeited') {
                toggleTurn(false, 'Your opponent forfeited!');
                showModal('User "' + opponentUsername + '" forfeited the game. You win!');
            } else if (message.action == 'text') {
                $("#game_msg").html("<div class='ui info message'>" + message.msg + "</div>" + $("#game_msg").html());
            }
        };

        var toggleTurn = function (isMyTurn, message) {
            myTurn = isMyTurn;
            if (myTurn) {
                status.text(message || 'It\'s your move!');
                $('.game-cell:not(.game-cell-taken)')
                        .addClass('game-cell-selectable');
            } else {
                status.text(message || 'Waiting on your opponent to move.');
                $('.game-cell-selectable')
                        .removeClass('game-cell-selectable');
            }
        };

        move = function (row, column) {
            if (!myTurn) {
                showModal('It is not your turn yet!');
                return;
            }
            if ($('.game-cell:eq(' + (row * 15 + column).toString() + ')').hasClass("game-cell-taken")) {
                showModal.html("已经有子了.");
                return;
            }
            if (server != null) {
                server.send(JSON.stringify({row: row, column: column}));
                $('#r' + row + 'c' + column).unbind('click')
                        .removeClass('game-cell-selectable')
                        .addClass('game-cell-player game-cell-taken');
                toggleTurn(false);
            } else {
                showModal('Not connected to came server.');
            }
        };
    });
</script>

<%@ include file="/footer.jsp" %>

</body>
</html>
