package org.zigmoi.ketchup.iam.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.zigmoi.ketchup.iam.common.AuthUtils;
import org.zigmoi.ketchup.iam.dtos.TenantDto;
import org.zigmoi.ketchup.iam.entities.Tenant;
import org.zigmoi.ketchup.iam.entities.User;
import org.zigmoi.ketchup.iam.repositories.TenantRepository;
import org.zigmoi.ketchup.iam.repositories.UserRepository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class TenantServiceImpl implements TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void createTenant(TenantDto tenantDto) {
        if (tenantRepository.findById(tenantDto.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s already exists.", tenantDto.getId()));
        }

        Tenant tenant = new Tenant();
        tenant.setId(tenantDto.getId());
        tenant.setDisplayName(tenantDto.getDisplayName());
        tenant.setEnabled(true);
        tenant.setCreationDate(new Date());
        tenantRepository.save(tenant);
        createDefaultUser(tenantDto.getId(), tenantDto.getDefaultUserEmail(), tenantDto.getDefaultUserPassword());
    }

    public void createDefaultUser(String tenantId, String userEmail, String userPassword) {
        if (AuthUtils.matchesPolicy(userPassword) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User Password %s is invalid!", userPassword));
        }
        User user = new User();
        user.setUserName("admin@".concat(tenantId));
        user.setTenantId(tenantId);
        user.setEmail(userEmail);
        user.setPassword(passwordEncoder.encode(userPassword));
        user.setEnabled(true);
        user.setCreationDate(new Date());
        user.setFirstName("Tenant Admin");
        user.setLastName("Tenant Admin");
        user.setDisplayName("Tenant Admin");
        HashSet roles = new HashSet<String>();
        roles.add("ROLE_TENANT_ADMIN");
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteTenant(String tenantId) {
        if (tenantRepository.findById(tenantId).isPresent()) {
            tenantRepository.deleteById(tenantId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId));
        }
    }

    @Override
    @Transactional
    public void updateTenantStatus(String tenantId, boolean status) {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId)));
        if (tenant.isEnabled() == status) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s has same status as requested.", tenantId));
        }
        tenant.setEnabled(status);
        tenantRepository.save(tenant);
    }

    @Override
    @Transactional
    public void updateTenantDisplayName(String tenantId, String displayName) {
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Tenant with id %s not found.", tenantId)));
        tenant.setDisplayName(displayName);
        tenantRepository.save(tenant);
    }

    @Override
    public Optional<Tenant> getTenant(String tenantId) {
        return tenantRepository.findById(tenantId);
    }

    @Override
    public List<Tenant> listAllTenants() {
        return tenantRepository.findAll();
    }

}
