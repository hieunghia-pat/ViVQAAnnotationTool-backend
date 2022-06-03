package uit.spring.annotation.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uit.spring.annotation.databases.User;
import uit.spring.annotation.security.UserDetails;
import uit.spring.annotation.repositories.UserRepository;

import java.util.Optional;

@Slf4j
@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty())
            throw new UsernameNotFoundException(String.format("User has %s as username cannot be found!", username));

        return user.map(UserDetails::new).get();
    }
}
