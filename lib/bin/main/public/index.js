var connected = true;

function refresh() {
    var progressCPU_A = $("#cpu_A");
    var textCPU_A = $("#cpu-text_A");
    var progressCPU_B = $("#cpu_B");
    var textCPU_B = $("#cpu-text_B");
    

    var progressRAM_A = $("#ram_A");
    var textRAM_A = $("#ram-text_A");
    var progressRAM_B = $("#ram_B");
    var textRAM_B = $("#ram-text_B");

    var progressTPS = $("#tps");
    var textTPS = $("#tps-text");
    var errorBar = $("#error-bar");

    if (connected) {
        $.ajax({
            url: "/stats",
            success: function (result) {
                errorBar.fadeOut();

                var result = JSON.parse(result);
                
                progressRAM_A.attr("max", "100");
                progressRAM_A.attr("value", (result.free_A / result.total_A) * 100 );
                textRAM_A.text("RAM: " + Math.round(result.free_A / 1024) + "MB / " + Math.round(result.total_A / 1024) + "MB");
                
                progressRAM_B.attr("max", "100");
                progressRAM_B.attr("value", (result.free_B / result.total_B) * 100 );
                textRAM_B.text("RAM: " + Math.round(result.free_B / 1024) + "MB / " + Math.round(result.total_B / 1024) + "MB");




                progressCPU_A.attr("max", "100");
                progressCPU_A.attr("value", result.cpu_A);
                textCPU_A.text("CPU usage A: " + result.cpu_A + "%");
                
                progressCPU_B.attr("max", "100");
                progressCPU_B.attr("value", result.cpu_B);
                textCPU_B.text("CPU usage B: " + Math.round(result.cpu_B) + "%");




                progressTPS.attr("max", "20");
                progressTPS.attr("value",  Math.round(result.tps));
                textTPS.text("TPS: " +  Math.round(result.tps));
            },
            error: function (result) {
                errorBar.text('Connection failed!');
                errorBar.fadeIn();

                setTimeout(function () {
                    connected = true;
                }, 10000);

                textCPU.text("CPU usage: ?");
                textTPS.text("TPS: ?");
                textRAM.text("RAM: ?");

                connected = false;
            }
        });
    }
}

function ansiformat(data) {
    var ansiTextArray = ansiparse(data);
    var text = "";

    for (i = 0; i < ansiTextArray.length; i++) {
        var data = ansiTextArray[i];
        if ('foreground' in data) {
            text += '<span style="color: ' + data.foreground + '">' + data.text + '</span>';
        } else {
            text += data.text;
        }
    }

    return text;
}


$(document).ready(function () {
    var scrollback;
    var fragment = document.createDocumentFragment();
    var term = $('#term-content');
    var termScroll = $('#term');

	var protocol = window.location.protocol != "https:" ? "ws" : "wss";

	var socket = new WebSocket(protocol + "://" + document.domain + ":" + window.location.port + "/socket");

	socket.onmessage = function (event) {
		if (event.data.contains("SCROLLBACK")) {
			dataArray = event.data.split(" ");
			scrollback = dataArray[1];
		}

		if (scrollback > 0) {
			var newLine = document.createElement('p');
			newLine.className = "term_line";
			newLine.innerHTML = ansiformat(event.data);
			fragment.appendChild(newLine);
			scrollback--;
		} else if (scrollback == 0) {
			var newLine = document.createElement('p');
			newLine.className = "term_line";
			newLine.innerHTML = ansiformat(event.data);
			fragment.appendChild(newLine);
			term.append(fragment);
			scrollback--;
            termScroll.scrollTop(termScroll.prop("scrollHeight"));
		} else {
			var newLine = document.createElement('p');
			newLine.className = "term_line";
			newLine.innerHTML = ansiformat(event.data);
			term.append(newLine);
            termScroll.scrollTop(termScroll.prop("scrollHeight"));
		}
	}

    $("#cmd_box").on('keypress', function (event) {
        if(event.which === 13){
            event.preventDefault()
            runCommand($(this).val());
            $(this).val("");
        }
    });

    $("#cmd_form").submit(function(){
        runCommand( $("#cmd_box").val());
        $("#cmd_box").val("");
        return false;
    });

    function runCommand(command) {
        if (command !== '') {
            if ((command == 'stop') || (command == 'reload') || command == 'end') {
                var cmd = command;
                if (confirm("Are you sure you want to " + cmd + " the server?")) {
                    socket.send(cmd);
                }
            } else {
                socket.send(command);
            }
        }
    }

    window.setInterval(function(){
        refresh();
    }, 1000);

});


// include ansi renderer
// Taken from: https://github.com/travis-ci/travis-web/blob/76af32013bc3ab1e5f540d69da3a97c3fec1e7e9/assets/scripts/vendor/ansiparse.js
var ansiparse = function (str) {
    //
    // I'm terrible at writing parsers.
    //
    var matchingControl = null,
        matchingData = null,
        matchingText = '',
        ansiState = [],
        result = [],
        state = {},
        eraseChar;

    //
    // General workflow for this thing is:
    // \033\[33mText
    // |     |  |
    // |     |  matchingText
    // |     matchingData
    // matchingControl
    //
    // In further steps we hope it's all going to be fine. It usually is.
    //

    //
    // Erases a char from the output
    //
    eraseChar = function () {
        var index, text;
        if (matchingText.length) {
            matchingText = matchingText.substr(0, matchingText.length - 1);
        }
        else if (result.length) {
            index = result.length - 1;
            text = result[index].text;
            if (text.length === 1) {
                //
                // A result bit was fully deleted, pop it out to simplify the final output
                //
                result.pop();
            }
            else {
                result[index].text = text.substr(0, text.length - 1);
            }
        }
    };

    for (var i = 0; i < str.length; i++) {
        if (matchingControl != null) {
            if (matchingControl == '\033' && str[i] == '\[') {
                //
                // We've matched full control code. Lets start matching formating data.
                //

                //
                // "emit" matched text with correct state
                //
                if (matchingText) {
                    state.text = matchingText;
                    result.push(state);
                    state = {};
                    matchingText = "";
                }

                matchingControl = null;
                matchingData = '';
            }
            else {
                //
                // We failed to match anything - most likely a bad control code. We
                // go back to matching regular strings.
                //
                matchingText += matchingControl + str[i];
                matchingControl = null;
            }
            continue;
        }
        else if (matchingData != null) {
            if (str[i] == ';') {
                //
                // `;` separates many formatting codes, for example: `\033[33;43m`
                // means that both `33` and `43` should be applied.
                //
                // TODO: this can be simplified by modifying state here.
                //
                ansiState.push(matchingData);
                matchingData = '';
            }
            else if (str[i] == 'm') {
                //
                // `m` finished whole formatting code. We can proceed to matching
                // formatted text.
                //
                ansiState.push(matchingData);
                matchingData = null;
                matchingText = '';

                //
                // Convert matched formatting data into user-friendly state object.
                //
                // TODO: DRY.
                //
                ansiState.forEach(function (ansiCode) {
                    if (ansiparse.foregroundColors[ansiCode]) {
                        state.foreground = ansiparse.foregroundColors[ansiCode];
                    }
                    else if (ansiparse.backgroundColors[ansiCode]) {
                        state.background = ansiparse.backgroundColors[ansiCode];
                    }
                    else if (ansiCode == 39) {
                        delete state.foreground;
                    }
                    else if (ansiCode == 49) {
                        delete state.background;
                    }
                    else if (ansiparse.styles[ansiCode]) {
                        state[ansiparse.styles[ansiCode]] = true;
                    }
                    else if (ansiCode == 22) {
                        state.bold = false;
                    }
                    else if (ansiCode == 23) {
                        state.italic = false;
                    }
                    else if (ansiCode == 24) {
                        state.underline = false;
                    }
                });
                ansiState = [];
            }
            else {
                matchingData += str[i];
            }
            continue;
        }

        if (str[i] == '\033') {
            matchingControl = str[i];
        }
        else if (str[i] == '\u0008') {
            eraseChar();
        }
        else {
            matchingText += str[i];
        }
    }

    if (matchingText) {
        state.text = matchingText + (matchingControl ? matchingControl : '');
        result.push(state);
    }
    return result;
};

ansiparse.foregroundColors = {
    '30': 'black',
    '31': 'red',
    '32': 'green',
    '33': 'yellow',
    '34': 'CornflowerBlue',
    '35': 'magenta',
    '36': 'cyan',
    '37': 'white',
    '90': 'grey'
};

ansiparse.backgroundColors = {
    '40': 'black',
    '41': 'red',
    '42': 'green',
    '43': 'yellow',
    '44': 'blue',
    '45': 'magenta',
    '46': 'cyan',
    '47': 'white'
};

ansiparse.styles = {
    '1': 'bold',
    '3': 'italic',
    '4': 'underline'
};

// yolo

String.prototype.contains = function(it) { return this.indexOf(it) != -1; };
