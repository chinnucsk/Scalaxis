%  @copyright 2009-2011 Zuse Institute Berlin

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

%% @author Florian Schintke <schintke@zib.de>
%% @doc    Supervisor for each DHT node that is responsible for keeping
%%         its transaction processes running.
%%
%%         If one of the supervised processes fails, it will be re-started!
%% @end
%% @version $Id$
-module(sup_dht_node_core_tx).
-author('schintke@zib.de').
-vsn('$Id$').

-behaviour(supervisor).

-export([start_link/1, init/1]).

-spec start_link(pid_groups:groupname())
        -> {ok, Pid::pid()} | ignore |
           {error, Error::{already_started, Pid::pid()} | shutdown | term()}.
start_link(DHTNodeGroup) ->
    supervisor:start_link(?MODULE, DHTNodeGroup).

-spec init(pid_groups:groupname()) ->
                  {ok, {{one_for_one, MaxRetries::pos_integer(),
                         PeriodInSeconds::pos_integer()},
                        [ProcessDescr::any()]}}.
init(DHTNodeGroup) ->
    pid_groups:join_as(DHTNodeGroup, ?MODULE),
    RDHT_tx_read = util:sup_worker_desc(rdht_tx_read, rdht_tx_read, start_link,
                                        [DHTNodeGroup]),
    RDHT_tx_write = util:sup_worker_desc(rdht_tx_write, rdht_tx_write,
                                         start_link, [DHTNodeGroup]),
    TX_TM = util:sup_worker_desc(tx_tm, tx_tm_rtm, start_link,
                                 [DHTNodeGroup, tx_tm]),
    TX_TM_Paxos = util:sup_supervisor_desc(
                    sup_paxos_tm, sup_paxos, start_link,
                    [DHTNodeGroup, [{sup_paxos_prefix, tx_tm}]]),
    TX_RTM0 = util:sup_worker_desc(tx_rtm0, tx_tm_rtm, start_link,
                                   [DHTNodeGroup, tx_rtm0]),
    TX_RTM0_Paxos = util:sup_supervisor_desc(
                sup_paxos_rtm0, sup_paxos, start_link,
                [DHTNodeGroup, [{sup_paxos_prefix, tx_rtm0}]]),
    TX_RTM1 = util:sup_worker_desc(tx_rtm1, tx_tm_rtm, start_link,
                                   [DHTNodeGroup, tx_rtm1]),
    TX_RTM1_Paxos = util:sup_supervisor_desc(
                    sup_paxos_rtm1, sup_paxos, start_link,
                    [DHTNodeGroup, [{sup_paxos_prefix, tx_rtm1}]]),
    TX_RTM2 = util:sup_worker_desc(tx_rtm2, tx_tm_rtm, start_link,
                                   [DHTNodeGroup, tx_rtm2]),
    TX_RTM2_Paxos = util:sup_supervisor_desc(
                    sup_paxos_rtm2, sup_paxos, start_link,
                    [DHTNodeGroup, [{sup_paxos_prefix, tx_rtm2}]]),
    TX_RTM3 = util:sup_worker_desc(tx_rtm3, tx_tm_rtm, start_link,
                                   [DHTNodeGroup, tx_rtm3]),
    TX_RTM3_Paxos = util:sup_supervisor_desc(
                    sup_paxos_rtm3, sup_paxos, start_link,
                    [DHTNodeGroup, [{sup_paxos_prefix, tx_rtm3}]]),
    {ok, {{one_for_one, 10, 1},
          [
           RDHT_tx_read, RDHT_tx_write,
           %% start paxos supervisors before tx processes to create used atoms
           TX_TM_Paxos, TX_TM,
           TX_RTM0_Paxos, TX_RTM0,
           TX_RTM1_Paxos, TX_RTM1,
           TX_RTM2_Paxos, TX_RTM2,
           TX_RTM3_Paxos, TX_RTM3
          ]}}.
