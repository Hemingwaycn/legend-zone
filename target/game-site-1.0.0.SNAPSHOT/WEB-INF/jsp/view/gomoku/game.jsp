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
    <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
    <script src="http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/js/bootstrap.min.js"></script>
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

    <div id="modalWaiting" class="modal hide fade">
        <div class="modal-header"><h3>Please Wait...</h3></div>
        <div class="modal-body" id="modalWaitingBody">&nbsp;</div>
    </div>
    <div id="modalError" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;
            </button>
            <h3>Error</h3>
        </div>
        <div class="modal-body" id="modalErrorBody">A blah error occurred.
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary" data-dismiss="modal">OK</button>
        </div>
    </div>
    <div id="modalGameOver" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;
            </button>
            <h3>Game Over</h3>
        </div>
        <div class="modal-body" id="modalGameOverBody">&nbsp;</div>
        <div class="modal-footer">
            <button class="btn btn-primary" data-dismiss="modal">OK</button>
        </div>
    </div>
</div>

<div class="ui small test modal transition hidden" style="margin-top: -92.5px;">
    <div class="header">Header </div>
    <div class="content">
        <p>Content</p>
    </div>
    <div class="actions">
        <!--<div class="ui negative button">No </div>-->
        <div class="ui positive right labeled icon button">OK <i class="checkmark icon"></i> </div>
    </div>
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
        var modalError = $("#modalError");
        var modalErrorBody = $("#modalErrorBody");
        var modalWaiting = $("#modalWaiting");
        var modalWaitingBody = $("#modalWaitingBody");
        var modalGameOver = $("#modalGameOver");
        var modalGameOverBody = $("#modalGameOverBody");
        var opponent = $("#opponent");
        var status = $("#status");
        var opponentUsername;
        var username = '<c:out value="${username}" />';
        var myTurn = false;

        $('.game-cell').addClass('span1');

        if (!("WebSocket" in window)) {
            modalErrorBody.text('WebSockets are not supported in this ' +
                    'browser. Try Internet Explorer 10 or the latest ' +
                    'versions of Mozilla Firefox or Google Chrome.');
            modalError.modal('show');
            return;
        }

        modalWaitingBody.text('Connecting to the server.');
        modalWaiting.modal({keyboard: false, show: true});

        var server;
        try {
            server = new WebSocket('ws://' + window.location.host +
                    '<c:url value="/gomoku/${gameId}/${username}">
            <c:param name="action" value="${action}" />
            </c:url>');
        } catch (error) {
            modalWaiting.modal('hide');
            modalErrorBody.text(error);
            modalError.modal('show');
            return;
        }

        server.onopen = function (event) {
            modalWaitingBody
                    .text('Waiting on your opponent to join the game.');
            modalWaiting.modal({keyboard: false, show: true});
        };

        window.onbeforeunload = function () {
            server.close();
        };

        server.onclose = function (event) {
            if (!event.wasClean || event.code != 1000) {
                toggleTurn(false, 'Game over due to error!');
                modalWaiting.modal('hide');
                modalErrorBody.text('Code ' + event.code + ': ' +
                        event.reason);
                modalError.modal('show');
            }
        };

        server.onerror = function (event) {
            modalWaiting.modal('hide');
            modalErrorBody.text(event.data);
            modalError.modal('show');
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
                modalWaiting.modal('hide');
            } else if (message.action == 'opponentMadeMove') {
                $('#r' + message.move.row + 'c' + message.move.column)
                        .unbind('click')
                        .removeClass('game-cell-selectable')
                        .addClass('game-cell-opponent game-cell-taken');
                toggleTurn(true);
            } else if (message.action == 'gameOver') {
                toggleTurn(false, 'Game Over!');

                $(".ui.modal").children(".header").html("Notice");



                if (message.winner) {
                    $(".ui.modal").children(".content").html("Congratulations, you won!");
                    //modalGameOverBody.text('Congratulations, you won!');
                } else {
                    //modalGameOverBody.text('User "' + opponentUsername +
                    //        '" won the game.');
                    $(".ui.modal").children(".content").html('传说大魔王是不可战胜的!');
                }
                $('.ui.modal').modal('show');
            } else if (message.action == 'gameIsDraw') {
                toggleTurn(false, 'The game is a draw. ' +
                        'There is no winner.');
                $(".ui.modal").children(".header").html("Notice");
                $(".ui.modal").children(".content").html('The game ended in a draw. ' + 'Nobody wins!');
                /*modalGameOverBody.text('The game ended in a draw. ' +
                        'Nobody wins!');
                modalGameOver.modal('show');*/
            } else if (message.action == 'gameForfeited') {
                toggleTurn(false, 'Your opponent forfeited!');

                $(".ui.modal").children(".header").html("Notice");
                $(".ui.modal").children(".content").html('User "' + opponentUsername + '" forfeited the game. You win!');

                /*modalGameOverBody.text('User "' + opponentUsername +
                        '" forfeited the game. You win!');*/
                modalGameOver.modal('show');
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
                $(".ui.modal").children(".header").html("Notice");
                $(".ui.modal").children(".content").html('It is not your turn yet!');
                /*modalErrorBody.text('It is not your turn yet!');
                modalError.modal('show');*/
                return;
            }
            if ($('.game-cell:eq(' + (row * 15 + column).toString() + ')').hasClass("game-cell-taken")) {
                $(".ui.modal").children(".header").html("Notice");
                $(".ui.modal").children(".content").html("已经有子了.");
                $('.ui.modal').modal('show');
                return;
            }
            if (server != null) {
                server.send(JSON.stringify({row: row, column: column}));
                $('#r' + row + 'c' + column).unbind('click')
                        .removeClass('game-cell-selectable')
                        .addClass('game-cell-player game-cell-taken');
                toggleTurn(false);
            } else {
                modalErrorBody.text('Not connected to came server.');
                modalError.modal('show');
            }
        };
    });
</script>

<%@ include file="/footer.jsp" %>

</body>

</html>
