jetty_base="/var/lib/jetty"

cookbook_file "webseal.conf" do
   path "#{jetty_base}/config/sdc-simulator/webseal.conf"
   source "webseal.conf"
   owner "jetty"
   group "jetty"
   mode "0755"
end