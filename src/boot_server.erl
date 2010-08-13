% @copyright 2007-2010 Konrad-Zuse-Zentrum fuer Informationstechnik Berlin

%   Licensed under the Apache License, Version 2.0 (the "License");
%   you may not use this file except in compliance with the License.
%   You may obtain a copy of the License at
%
%       http://www.apache.org/licenses/LICENSE-2.0
%
%   Unless required by applicable law or agreed to in writing, software
%   distributed under the License is distributed on an "AS IS" BASIS,
%   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%   See the License for the specific language governing permissions and
%   limitations under the License.

%% @author Thorsten Schuett <schuett@zib.de>
%% @doc The boot server maintains a list of scalaris nodes and checks their 
%%      availability using a failure_detector. Its main purpose is to 
%%      give new scalaris nodes a list of nodes already in the system.
%% @end
-module(boot_server).
-author('schuett@zib.de').
-vsn('$Id$').

-export([start_link/0,
         number_of_nodes/0,
         node_list/0,
         connect/0]).

-behaviour(gen_component).
-include("scalaris.hrl").

-export([init/1, on/2]).

% accepted messages of the boot_server process
-type(message() ::
    {crash, PID::comm:mypid()} |
    {get_list, Ping_PID::comm:mypid()} |
    {be_the_first, Ping_PID::comm:mypid()} |
    {get_list_length, Ping_PID::comm:mypid()} |
    {register, Ping_PID::comm:mypid()} |
    {connect}).

% internal state
-type(state()::{Nodes::gb_set() % known nodes
               }).

%% @doc trigger a message with  the number of nodes known to the boot server
-spec number_of_nodes() -> ok.
number_of_nodes() ->
    comm:send(bootPid(), {get_list_length, comm:this()}),
    ok.

-spec connect() -> ok.
connect() ->
    % @todo we have to improve the startup process!
    comm:send(bootPid(), {connect}).

%% @doc trigger a message with all nodes known to the boot server
-spec node_list() -> ok.
node_list() ->
    comm:send(bootPid(), {get_list, comm:this()}).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Implementation
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

-spec on(message(), state()) -> state().
on({crash, PID}, {Nodes}) ->
    NewNodes = gb_sets:delete_any(PID, Nodes),
    {NewNodes};

on({get_list, Ping_PID}, {Nodes} = State) ->
    comm:send(Ping_PID, {get_list_response, gb_sets:to_list(Nodes)}),
    State;

on({get_list_length, Ping_PID}, {Nodes} = State) ->
    comm:send(Ping_PID, {get_list_length_response, length(gb_sets:to_list(Nodes))}),
    State;

on({register, Ping_PID}, {Nodes}) ->
    fd:subscribe(Ping_PID),
    NewNodes = gb_sets:add(Ping_PID, Nodes),
    {NewNodes};

on({connect}, State) ->
    % ugly work around for finding the local ip by setting up a socket first
    State.

-spec init([]) -> state().
init(_Arg) ->
    log:log(info,"[ Boot | ~w ] Starting Bootserver",[self()]),
    case application:get_env(boot_cs, empty) of
        {ok, true} ->
            % ugly hack to get a valid ip-address into the comm-layer
            dht_node:trigger_known_nodes();
        _ ->
            ok
    end,
    {gb_sets:empty()}.

%% @doc starts the server; called by the boot supervisor
%% @see sup_scalaris
-spec start_link() -> {ok, pid()}.
start_link() ->
     gen_component:start_link(?MODULE, [], [{register_native, boot}]).

%% @doc pid of the boot daemon
-spec bootPid() -> comm:mypid().
bootPid() ->
    config:read(boot_host).
