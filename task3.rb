# 209.17.97.2 - - [03/Oct/2019:19:48:35 +0000] "GET / HTTP/1.1" 200 160 "-" "Mozilla/5.0 (compatible; Nimbostratus-Bot/v1.3.2; http://cloudsystemnetworks.com)"
# %h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\"
# host identity(RFC1413) userid(httpauth) date reguest statuscode size "referer" "uesragent"
regexp = /^
		((\d{1,3}\.){3}\d{1,3})			# IP
		\s\-							# RFC 1413 identity (not used)
		\s\- 							# HTTP Basic Auth userid (not used)
		\s(\[.+?\])						# date
		\s"(GET|POST)\s(.+?)\s.+?"		# request (method, path, version)
		\s(\d+)							# status
		\s(\d+)							# answer size
		\s"(.+?)"						# referer
		\s"(.+?)"						# user-agent
		$/x

def extract(line, regexp)
	m = line.match(regexp)
	res = Hash.new
	if m then
		res['ip'], _,
		res['date'],
		res['method'], res['path'],
		res['status'],
		res['size'],
		res['referer'],
		res['user-agent'] = m.captures
		res
	end
end

def process(data, clients, paths)
	client = data['ip']
	if !clients[client] then clients[client] = 0 end
	clients[client] += 1

	path = data['path']
	if !paths[path] then paths[path] = 0 end
	paths[path] += 1
	
end

paths = Hash.new
clients = Hash.new

STDIN.readlines.each { |line|
	data = extract(line, regexp)
	if data then process(data, clients, paths) end
}

puts "Top 20 clients"
(clients.sort_by {|key, value| -value})[0,20].map {|ip, num|
	puts "#{ip} -> #{num}"
}
puts
puts "Top 20 paths"
(paths.sort_by {|key, value| -value})[0,20].map {|path, num|
	puts "#{path} -> #{num}"
}