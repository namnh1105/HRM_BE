package com.hainam.worksphere.auth.security;

import com.hainam.worksphere.shared.exception.UserNotFoundException;
import com.hainam.worksphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmailAndIsEnabledTrue(email)
                .map(UserPrincipal::create)
                .orElseThrow(() -> UserNotFoundException.byEmail(email));
    }
}
