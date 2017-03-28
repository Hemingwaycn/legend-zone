<%--@elvariable id="action" type="java.lang.String"--%>
<%--@elvariable id="gameId" type="long"--%>
<%--@elvariable id="username" type="java.lang.String"--%>
<!DOCTYPE html>
<html>
    <head>
        <link rel="shortcut icon" href="resource/image/logo.png">
        <title>LegendZone :: Scripts And Scribes</title>
        <link rel="stylesheet" href="http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/css/bootstrap.min.css" />
        <link rel="stylesheet"
              href="<c:url value="/resource/stylesheet/ticTacToe.css" />" />
        <script src="http://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/js/bootstrap.min.js"></script>
    </head>
    <body>
    <%@ include file="/header.jsp"%>
        <div class="ui main text container">
        <h2>Scripts And Scribes</h2>

            <div class="two ui buttons">
                <button class="ui green button"><span class="player-label"></span> ${username}</button>
                <div class="or" data-text="vs"></div>
                <button class="ui red loading button"><span class="player-label"></span><span id="opponent"></span></button>
            </div>

            <div class="ui warning message">"既驰三辈毕,而传说一不胜而再胜,卒得千金...遂以为魔王." -- 传说大魔王赛马
            </br>
                游戏规则:每人拥有9张牌,分别为数字1~9.每回合双方同时出牌,数字大的一方是本回合的胜利方.9回合获胜次数多的一方胜.
            </div>
            <div class="ui warning message"><div id="status">&nbsp;</div></div>




        <div id="gameContainer">
            <div class="row">
                <div id="r0c0" class="game-cell" onclick="move(0, 0);">1&nbsp;</div>
                <div id="r0c1" class="game-cell" onclick="move(0, 1);">2&nbsp;</div>
                <div id="r0c2" class="game-cell" onclick="move(0, 2);">3&nbsp;</div>
                <div id="r0c3" class="game-cell" onclick="move(0, 3);">4&nbsp;</div>
                <div id="r0c4" class="game-cell" onclick="move(0, 4);">5&nbsp;</div>
                <div id="r0c5" class="game-cell" onclick="move(0, 5);">6&nbsp;</div>
                <div id="r0c6" class="game-cell" onclick="move(0, 6);">7&nbsp;</div>
                <div id="r0c7" class="game-cell" onclick="move(0, 7);">8&nbsp;</div>
                <div id="r0c8" class="game-cell" onclick="move(0, 8);">9&nbsp;</div>
            </div>

        </div>

        <div id="game_msg"></div>


        </div>
        <script type="text/javascript" language="javascript">
            var move;
            $(document).ready(function() {
                var opponent = $("#opponent");
                var status = $("#status");
                var opponentUsername;
                var username = '<c:out value="${username}" />';
                var myTurn = false;

                $('.game-cell').addClass('span1');

                if(!("WebSocket" in window))
                {
                    showModal('WebSockets are not supported in this ' +
                            'browser. Try Internet Explorer 10 or the latest ' +
                            'versions of Mozilla Firefox or Google Chrome.');
                    return;
                }

                var server;
                try {
                    server = new WebSocket('ws://' + window.location.host +
                            '<c:url value="/scriptsAndScribes/${gameId}/${username}">
                                <c:param name="action" value="${action}" />
                            </c:url>');
                } catch(error) {
                    showModal(error);
                    return;
                }

                server.onopen = function(event) {

                };

                window.onbeforeunload = function() {
                    server.close();
                };

                server.onclose = function(event) {
                    if(!event.wasClean || event.code != 1000) {
                        toggleTurn(false, 'Game over due to error!');
                        showModal('Code ' + event.code + ': ' +
                                event.reason);
                    }
                };

                server.onerror = function(event) {
                    showModal(event.data);
                };

                server.onmessage = function(event) {
                    var message = JSON.parse(event.data);
                    if(message.action == 'gameStarted') {
                        if (message.game.player1 == username)
                            opponentUsername = message.game.player2;
                        else
                            opponentUsername = message.game.player1;
                        opponent.text(opponentUsername);
                        $(".loading").removeClass("loading");;
                        //toggleTurn(message.game.nextMoveBy == username);
                        toggleTurn(true);


                    } else if(message.action == 'opponentMadeMove') {
                        /*$('#r' + message.move.row + 'c' + message.move.column)
                                .unbind('click')
                                .removeClass('game-cell-selectable')
                                .addClass('game-cell-opponent game-cell-taken');*/
                        toggleTurn(true);
                    } else if(message.action == 'gameOver') {
                        toggleTurn(false, 'Game Over!');
                        if(message.winner) {
                            showModal('Congratulations, you won!');
                        } else {
                            showModal('User "' + opponentUsername +
                                    '" won the game.');
                        }
                    } else if(message.action == 'gameIsDraw') {
                        toggleTurn(false, 'The game is a draw. ' +
                                'There is no winner.');
                        showModal('The game ended in a draw. ' +
                                'Nobody wins!');
                    } else if(message.action == 'gameForfeited') {
                        toggleTurn(false, 'Your opponent forfeited!');
                        showModal('User "' + opponentUsername +
                                '" forfeited the game. You win!');
                    } else if(message.action == 'text'){
                        $("#game_msg").html("<div class='ui info message'>" + message.msg + "</div>" + $("#game_msg").html());
                    }
                };

                var toggleTurn = function(isMyTurn, message) {
                    myTurn = isMyTurn;
                    if(myTurn) {
                        status.text(message || 'It\'s your move!');
                        $('.game-cell:not(.game-cell-taken)')
                                .addClass('game-cell-selectable');
                    } else {
                        status.text(message ||'Waiting on your opponent to move.');
                        $('.game-cell-selectable')
                                .removeClass('game-cell-selectable');
                    }
                };

                move = function(row, column) {
                    if(!myTurn) {
                        showModal('It is not your turn yet!');
                        return;
                    }
                    if($('.game-cell:eq('+column+')').hasClass("game-cell-taken")){
                        showModal('This number was already used!');
                        return;
                    }
                    if(server != null) {
                        server.send(JSON.stringify({ row: row, column: column }));
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

        <%@ include file="/footer.jsp"%>

    </body>

</html>
