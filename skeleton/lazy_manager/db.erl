%%
%% Joey Zhao (zhaoyi.zero@gmail.com)
%% 2013-09-04
%%

-module (db).
-author ('zhaoyi.zero@gmail.com').

-compile (export_all).

-include ("db.hrl").

-define (DATABASE_NAME, "test_database").
-define (TABLE_NAME, "test_table").

% create database <db_name>
create_database(Name) ->
	#db_database{id = -1, name = Name, tables = []}.

% create table <table_name>
create_table(Name, Columns) ->
	#db_table{name = Name, columns = Columns, rows = []}.
% create_table(#db_database{tables = Tables} = Database, Name, Columns) ->
% 	case find_table(Tables, Name) of
% 		{error, _} ->
% 			NewDatabase =
% 				Database#db_database {
% 					tables = [#db_table{name = Name, columns = Columns} | Tables]
% 				},
% 			{ok, NewDatabase};
% 		_ ->
% 			{error, none}
% 	end.
add_table(#db_database{tables = Tables} = Database, #db_table{name = Name} = Table) ->
	case find_table(Database, Name) of
		{error, _} ->
			{ok, Database#db_database{tables = [Table | Tables]}};
		_ ->
			{error, none}
	end.

find_table(#db_database{tables = Tables} = _, Name) ->
	find_table(Tables, Name);
find_table([#db_table{name = TableName} = Table | _], Name)
	when TableName == Name ->
	{ok, Table};
find_table([], _) ->
	{error, none}.

create_row(Id, Values) ->
	#db_row{id = Id, values = Values}.

% insert xxx into <table_name>
insert (#db_table{rows = Rows} = Table, Values) ->
	Table#db_table{rows = [#db_row{values = Values} | Rows]}.
% insert(#db_table{rows = Rows} = Table, Row) when is_record(Row, db_row) ->
% 	Table#db_table{rows = [Row | Rows]}.

select (Columns, Rows) ->
	true.

where (TableName, Conds, #db_database{tables = Tables} = _) ->
	case find_table(Tables, TableName) of
		{ok, Table} ->
			filter (Table, Conds, []);
		_ ->
			[]
	end.

filter (#db_table{rows = [Row | LeftRows]} = Table, Conds, Result) ->
	case match_row(Table, Row, Conds) of
		true ->
			filter(Table#db_table{rows = LeftRows}, Conds, [Row | Result]);
		_ ->
			filter(Table#db_table{rows = LeftRows}, Conds, Result)
	end;
filter (#db_table{rows = []} = Table, _, Result) ->
	Result.

match_row (#db_table{columns = Columns} = Table, #db_row{values = Values} = Row, [FirstCond | LeftConds]) ->
	case match_cond(Columns, Values, FirstCond) of
		true ->
			match_row(Table, Row, LeftConds);
		_ ->
			false
	end;
match_row (_, _, []) ->
	true;
match_row (_, _, _) ->
	false.

match_cond ([FirstColumn | LeftColumns], [FirstValue | LeftValues], {CondColumn, CondValue} = Cond) ->
	if
		(FirstColumn == CondColumn) and (FirstValue == CondValue) ->
			true;
		true ->
			match_cond (LeftColumns, LeftValues, Cond)
	end;
match_cond (_, _, _) ->
	false.

test1(Result) ->
	Result.

test () ->
	Database = create_database(?DATABASE_NAME),
	Table = insert(create_table(?TABLE_NAME, ["f1", "f2", "f3"]), [1,2,3]),
	% add_table (Database, Table).
	case add_table(Database, Table) of
		{ok, NewDatabase} ->
			where(Table#db_table.name, [{"f1", 1}], NewDatabase);
		_ ->
			error
	end.

	% select * from table test_table where f1 == 1
	% select(
	% 	["f1", "f2"],
	% 	where(
	% 		?TABLE_NAME,
	% 		[{"f1", 1}, {"f2", 2}],
	% 		Database)).
	% Table = create_table(Data),
	% insert(Database, 
