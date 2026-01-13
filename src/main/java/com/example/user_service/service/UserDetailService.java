package com.example.user_service.service;

import com.example.user_service.model.Account;
import com.example.user_service.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService implements UserDetailsService {
    private AccountRepository accountRepository;
    public UserDetailService(AccountRepository accountRepository){
        this.accountRepository= accountRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account acc= accountRepository.findByUserName(username)
                .orElseThrow(()-> new RuntimeException("User not found"));
        GrantedAuthority role= new SimpleGrantedAuthority("ROLE_"+acc.getRole());
        return new User(
                acc.getUserName(),
                acc.getPassword(),
                java.util.Collections.singletonList(role)
        );
    }
}
