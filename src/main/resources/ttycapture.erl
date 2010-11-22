%%%-----------------------------------------------------------------------------
%%% @author Richard Carlsson <richardc@it.uu.se>
%%% @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
%%% @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
%%% @doc
%%% An {@link eunit_listener} capturing {@link eunit_tty}-like output into a
%%% list of lines, sending them to a specific process. This is based on the
%%% eunit_tty.erl module provided along with the standard erlang/OTP
%%% distribution. License is LGPL.
%%% @end
%%% @copyright 2006-2009 Richard Carlsson
%%% Created : 22 Nov 2010
%%%-----------------------------------------------------------------------------

-module(ttycapture).

-behaviour(eunit_listener).

-export([start/0,
	 start/1,
	 init/1,
	 handle_begin/3,
	 handle_end/3,
	 handle_cancel/3,
	 terminate/2]).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% record definition section

-record(state, {
	  report_to       :: pid(),
	  lines   = []    :: list(string())}).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% public function section

%%%-----------------------------------------------------------------------------
%%% @doc
%%% Initialize this module.
%%% @end
%%%-----------------------------------------------------------------------------
start() ->
    start([]).

%%%-----------------------------------------------------------------------------
%%% @doc
%%% Initialize this module with options.
%%% @end
%%%-----------------------------------------------------------------------------
start(Options) ->
    eunit_listener:start(?MODULE, Options).

%%%-----------------------------------------------------------------------------
%%% @doc
%%% Initialize this module with options. A pid to report to must be given!
%%% @end
%%%-----------------------------------------------------------------------------
init([{report_to, Pid}]) ->
    #state{report_to = Pid}.

%%%-----------------------------------------------------------------------------
%%% @doc
%%% Handles the begin of a test case or suite.
%%% @end
%%%-----------------------------------------------------------------------------
handle_begin(_, _, State) ->
    State.

%%%-----------------------------------------------------------------------------
%%% @doc
%%% Handles the end of a test case or suite.
%%% @end
%%%-----------------------------------------------------------------------------
handle_end(test, Data, St = #state{lines = Lines}) ->
    case proplists:get_value(status, Data) of
	ok -> St;
	Status ->
	    St#state{lines = Lines ++ print_test_error(Status, Data)}
    end;
handle_end(_, _, State) ->
    State.

%%%-----------------------------------------------------------------------------
%%% @doc
%%% Handles the cancellation of a test case or suite.
%%% @end
%%%-----------------------------------------------------------------------------
handle_cancel(group, Data, St = #state{lines = Lines}) ->
    case proplists:get_value(reason, Data) of
	undefined -> St;
	{blame, _} -> St;
	Reason ->
	    Desc = proplists:get_value(desc, Data),
	    St#state{lines = Lines
		     ++ [format("~s", [Desc])]
		     ++ format_cancel(Reason)}
    end;
handle_cancel(test, Data, St = #state{lines = Lines}) ->
    Reason = proplists:get_value(reason, Data),
    St#state{lines = Lines ++ format_cancel(Reason)}.

%%%-----------------------------------------------------------------------------
%%% @doc
%%% Sends the collected string lines to a specific pid.
%%% @end
%%%-----------------------------------------------------------------------------
terminate({ok, Data}, #state{lines = Lines, report_to = Dest}) ->
    io:format("~p", [Data]),
    Pass = proplists:get_value(pass, Data, 0),
    Fail = proplists:get_value(fail, Data, 0),
    Skip = proplists:get_value(skip, Data, 0),
    Cancel = proplists:get_value(cancel, Data, 0),
    Dest ! format_result(Pass, Fail, Skip, Cancel, Lines);
terminate({error, Reason}, #state{lines = Lines, report_to = Dest}) ->
    Dest ! {error, Lines ++ [format("Internal error: ~p.\n", [Reason])]}.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% internal function section

%%%-----------------------------------------------------------------------------
%%% @doc
%%% @end
%%%-----------------------------------------------------------------------------
format_result(0, 0, 0, 0, Acc) ->
    {warn, Acc ++ ["  There were no tests to run."]};
format_result(1, 0, 0, 0, Acc) ->
    {info, Acc ++ ["  Test passed."]};
format_result(Pass, 0, 0, 0, Acc) ->
    {info, Acc ++ [format("  All ~w tests passed.", [Pass])]};
format_result(Pass, Fail, Skip, 0, Acc) ->
    {error, Acc ++
     ["=======================================================",
      format("  Failed: ~w.  Skipped: ~w.  Passed: ~w.", 
	     [Fail, Skip, Pass])]};
format_result(Pass, Fail, Skip, Cancel, Acc) ->
    {error, Acc ++
     ["=======================================================",
      format("  Failed: ~w.  Skipped: ~w.  Passed: ~w  Cancelled: ~w.",
	     [Fail, Skip, Pass, Cancel])]}.

%%%-----------------------------------------------------------------------------
%%% @doc
%%% @end
%%%-----------------------------------------------------------------------------
print_test_error({error, Exception}, Data) ->
    O = case proplists:get_value(output, Data) of
	    undefined -> [""];
	    <<>> -> [""];
	    Else -> [format("  output:<<\"~w\">>", [Else]), ""]
	end,
    E = format("::~s", [eunit_lib:format_exception(Exception)]),
    ["*failed*", E] ++ O;
print_test_error({skipped, Reason}, _) ->
    ["*did not run*"] ++ format_skipped(Reason).

%%%-----------------------------------------------------------------------------
%%% @doc
%%% @end
%%%-----------------------------------------------------------------------------
format_skipped({module_not_found, M}) ->
    [format("::missing module: ~w", [M]), ""];
format_skipped({no_such_function, {M, F, A}}) ->
    [format("::no such function: ~w:~w/~w", [M, F, A]), ""].

%%%-----------------------------------------------------------------------------
%%% @doc
%%% @end
%%%-----------------------------------------------------------------------------
format_cancel(undefined) ->
    ["*skipped*"];
format_cancel(timeout) ->
    ["*timed out*"];
format_cancel({startup, Reason}) ->
    ["*could not start test process*", format("::~p", [Reason]), ""];
format_cancel({blame, _}) ->
    ["*cancelled because of subtask*"];
format_cancel({exit, Reason}) ->
    ["*unexpected termination of test process*", format("::~p", [Reason]), ""];
format_cancel({abort, Reason}) ->
    ["*eunit error*", format("::~s", [eunit_lib:format_error(Reason)]), ""].

%%%-----------------------------------------------------------------------------
%%% @doc
%%% @end
%%%-----------------------------------------------------------------------------
format(FormatString, FormatArgs) ->
    lists:flatten(io_lib:format(FormatString, FormatArgs)).
