MERGE INTO sftp_config (client_key, host, port, username, password, remote_directory, description) KEY (client_key)
VALUES 
  ('client_abc', 'sftp.bankabc.com', 22, 'userabc', 'passabc', '/upload/abc', 'Bank ABC SFTP'),
  ('client_xyz', 'sftp.bankxyz.net', 22, 'userxyz', 'passxyz', '/files/xyz', 'Bank XYZ SFTP'),
  ('client_demo', 'sftp.demo.io', 2022, 'demouser', 'demopass', '/incoming', 'Demo SFTP for testing'),
  ('client_alacriti', 'sftp.alacriti.com', 22, 'alacriti_user', 'alacriti_pass', '/ach_upload', 'Alacriti test SFTP'),
  ('client_test', 'test-sftp.com', 2222, 'testuser', 'testpass', '/remote', 'Testing SFTP server'),
  ('client_real','eu-central-1.sftpcloud.io',22,'bc88f065c4c64fa8bee775fb86387dbe','yjqsQNkrQgfaCQEnrn4unjR2vIAalRSu','/test','Working sftp server');
