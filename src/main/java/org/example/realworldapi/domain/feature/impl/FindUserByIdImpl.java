package org.example.realworldapi.domain.feature.impl;

import jakarta.inject.Inject;
import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.feature.FindUserById;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.domain.model.user.UserRepository;

import java.util.UUID;

public class FindUserByIdImpl implements FindUserById {

    @Inject
    private UserRepository userRepository;

    @Override
    public User handle(UUID id) {
        return userRepository.findUserById(id).orElseThrow(UserNotFoundException::new);
    }
}
