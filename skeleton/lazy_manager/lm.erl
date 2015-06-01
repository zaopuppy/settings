%%
%% Joey Zhao (zhaoyi.zero@gmail.com)
%% 2013-05-27
%%

-module (lm).
-author ('zhaoyi.zero@gmail.com').
-import (db, [new/0]).

-include_lib ("kernel/include/file.hrl").

-export ([start/0, test/0, scan_dir/1]).

% -record(person, {name,age=0,phone}).

start() ->
	db:new().
	% scan_dir(FileName).

scan_dir(FileName) ->
	scan_dir_r([FileName], 0).

scan_dir_r([First|Left], Count) ->
	case file:read_file_info(First) of
		{ok, FileInfo} ->
			if
				FileInfo#file_info.type == directory ->
					% io:format("d: ~p~n", [First]),
					case file:list_dir_all(First) of
						{ok, FileNames} ->
							scan_dir_r([ First ++ "/" ++ X || X <- FileNames] ++ Left, Count + 1);
						{_, Reason} ->
							io:format("Failed to list dir [~p], reason [~p]~n", [First, Reason]),
							scan_dir_r(Left, Count + 1)
							% {error, -1}
					end;
				true ->
					% io:format("-: ~p~n", [First]),
					scan_dir_r(Left, Count + 1)
			end;
		{_, Reason} ->
			io:format("Failed to read file info [~p], reason [~p]~n", [First, Reason]),
			scan_dir_r(Left, Count + 1)
			% {error, -1}
	end;

scan_dir_r([], Count) ->
	{ok, Count}.

test() ->
	Result = reverse_string("1234567", []),
	io:format("~p~n", [Result]).

reverse_string([], Result) ->
	Result;
reverse_string([Head|Tail], Result) ->
	reverse_string(Tail, [Head|Result]).

