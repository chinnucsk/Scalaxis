#!/usr/bin/ruby

require 'rubygems'
require 'sinatra'
require 'sequel'
require 'erb'
require 'optparse'
require 'json'

require 'one.rb'
require 'database.rb'
require 'jsonrpc.rb'
require 'scalarishelper.rb'
require 'hadoophelper.rb'

set :views, Proc.new { File.join(root, "sc_views") }

helper = nil
instance = nil
vmid = -1

if settings.test?
  vmid = ENV['VMID']
  helper = ScalarisHelper.new

  instance = Service.create(:master_node => vmid)
  instance.add_vm(:one_vm_id => vmid)
else
  if TYPE == "scalaris"
    helper = ScalarisHelper.new
  elsif TYPE == "hadoop"
    helper = HadoopHelper.new
  else
    exit
  end

  vmid = ENV['VMID']
  instance = Service.first(:master_node => vmid)
  if instance == nil
    instance = Service.create(:master_node => vmid)
    instance.add_vm(:one_vm_id => vmid)
  end
  puts instance.id
end


get '/' do
  if TYPE == "hadoop"
    erb :hadoop_index
  else
    erb :scalaris_index
  end
end

post '/add' do
  res = helper.add(1, instance)
  if res[0] == true
    @url = res[1]
    @error = ""
  else
    @url = ""
    @error = res[1]
  end
  erb :add
end

post '/list' do
  @list = helper.list(instance)
  erb :list
end

get '/jsonrpc' do
  redirect "/"
end

if TYPE == "hadoop"
  post '/analyze_logs' do
    system("perl /root/analyze_logs.pl")
    erb :hadoop_waitresults
  end
  get '/analyze_logs' do
    erb :hadoop_waitresults
  end
end

get '/jsonrpc' do
  redirect "/"
end

post '/jsonrpc' do
  req = JSON.parse(request.body.read)
  res = JSONRPC.call(req, helper, instance)
  puts res
  res
end
