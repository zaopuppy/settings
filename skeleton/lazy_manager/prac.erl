-module (prac).

-compile([export_all]).

test() ->
  Set = lists:seq(1, 3),
  p1(Set, Set, []).


% -----------------------------------------------
% permutation: 1
p1(_, [], R) ->
  R;
p1([_ | []] = Set, _, R) ->
  [Set | R];
p1(Set, [E | L], R) ->
  S = p1(Set -- [E], Set -- [E], []),
  SS = lists:foldl(fun(X, Res) -> [[E | X] | Res] end, [], S),
  io:format("E=~p, R=~p, S=~p, SS=~p~n", [E, R, S, SS]),
  p1(Set, L, SS ++ R).

% permutation: 2
p2([]) ->
  [];
p2([_ | []] = Set) ->
  [Set];
p2(Set) ->
  lists:foldl(
    fun(X1, R1) ->
      S = p2(Set -- [X1]),
      SS = lists:foldl(fun(X2, R2) -> [[X1 | X2] | R2] end, [], S),
      SS ++ R1
    end,
    [],
    Set).

