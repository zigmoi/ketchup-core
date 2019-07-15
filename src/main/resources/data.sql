TRUNCATE TABLE users;
INSERT INTO `users` (`username`, `email`, `enabled`, `firstname`, `lastname`, `password`, `role`) VALUES ('sa', 'test@gmail.com', '1', 'admin', 'admin', '$2a$04$dvoJcMTDPkaQvB0slTdMBOLdinrh26jFpLZk1t04/fVJTblWtd736', 'ROLE_ADMIN');
commit;