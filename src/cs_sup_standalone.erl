%  Copyright 2007-2008 Konrad-Zuse-Zentrum für Informationstechnik Berlin
%
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
%%%-------------------------------------------------------------------
%%% File    : cs_sup_standalone.erl
%%% Author  : Thorsten Schuett <schuett@csr-pc11.zib.de>
%%% Description : Supervisor for "standalone" mode
%%%
%%% Created : 17 Aug 2007 by Thorsten Schuett <schuett@csr-pc11.zib.de>
%%%-------------------------------------------------------------------
-module(cs_sup_standalone).

-behaviour(supervisor).

%% API
-export([start_link/0]).

%% Supervisor callbacks
-export([init/1]).

-define(SERVER, ?MODULE).

%%====================================================================
%% API functions
%%====================================================================
%%--------------------------------------------------------------------
%% Function: start_link() -> {ok,Pid} | ignore | {error,Error}
%% Description: Starts the supervisor
%%--------------------------------------------------------------------
start_link() ->
    Link = supervisor:start_link({local, main_sup}, ?MODULE, []),
    scan_environment(),
    Link.

%%====================================================================
%% Supervisor callbacks
%%====================================================================
%%--------------------------------------------------------------------
%% Func: init(Args) -> {ok,  {SupFlags,  [ChildSpec]}} |
%%                     ignore                          |
%%                     {error, Reason}
%% Description: Whenever a supervisor is started using 
%% supervisor:start_link/[2,3], this function is called by the new process 
%% to find out about restart strategy, maximum restart frequency and child 
%% specifications.
%%--------------------------------------------------------------------
init([]) ->
    crypto:start(),
    inets:start(),
    util:logger(),
    error_logger:logfile({open, "cs.log"}),
    Config =
	{config,
	 {config, start_link, [["scalaris.cfg", "scalaris.local.cfg"]]},
	 permanent,
	 brutal_kill,
	 worker,
	 []},
    CommunicationPort = {
      comm_port,
      {comm_layer.comm_layer, start_link, []},
      permanent,
      brutal_kill,
      worker,
      []
     },
    ChordSharp = 
	{chordsharp,
	 {cs_sup_or, start_link, []},
	 permanent,
	 brutal_kill,
	 supervisor,
	 [cs_sup_or]
     },
    YAWS = 
	{yaws,
	 {yaws_wrapper, try_link, ["../docroot_node", 
				     [{port, 8001}, {listen, {0,0,0,0}}], 
				     [{max_open_conns, 800}, {access_log, false}]
				    ]},
	 permanent,
	 brutal_kill,
	 worker,
	 []},
    {ok,{{one_for_all,10,1}, [
			      Config,
			      CommunicationPort,
			      YAWS,
			      ChordSharp
			     ]}}.

%%====================================================================
%% Internal functions
%%====================================================================

scan_environment() ->
    loadInstances(os:getenv("CS_INSTANCES")),
    ok.

loadInstances(false) ->
    ok;
loadInstances(Instances) ->
    {Int, []} = string:to_integer(Instances),
    admin:add_nodes(Int - 1).
