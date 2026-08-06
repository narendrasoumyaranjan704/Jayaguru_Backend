package com.temple.donation.service;

import com.temple.donation.entity.AppUser;
import com.temple.donation.entity.Donation;
import com.temple.donation.entity.DonationSheet;
import com.temple.donation.repository.AppUserRepository;
import com.temple.donation.repository.DonationSheetRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SeedService {

    private final DonationSheetRepository sheetRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedService(DonationSheetRepository sheetRepository,
                       AppUserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.sheetRepository = sheetRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void ensureSeed() {
        seedUsers();
        if (sheetRepository.count() == 0) {
            seedSheets();
        }
    }

    @Transactional
    public void seedSheets() {
        YearMonth now = YearMonth.now();
        for (int back = 5; back >= 0; back--) {
            YearMonth ym = now.minusMonths(back);
            int year = ym.getYear();
            int month = ym.getMonthValue();
            int daysInMonth = ym.lengthOfMonth();
            String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            DonationSheet sheet = new DonationSheet();
            sheet.setName(monthName + " " + year + " Donations");
            sheet.setYear(year);
            sheet.setMonth(month);
            sheet.setNotes("Sample data - add / edit / delete freely");

            for (Map.Entry<String, List<String>> entry : OCCASION_SETS.entrySet()) {
                String devotee = entry.getKey();
                List<String> occasions = entry.getValue();
                int nameLen = devotee.length();
                int useCount = 1 + ((back + nameLen) % 2);

                for (int i = 0; i < useCount; i++) {
                    String occasion = occasions.get((back + i) % occasions.size());
                    int amount = 200 + ((nameLen * 137 + back * 53 + i * 29) % 1801);
                    int day = 3 + ((back * 7 + i * 5 + nameLen) % (daysInMonth - 5));
                    long mobileBase = 100000000L + ((nameLen * 11111111L) % 900000000L);

                    Donation donation = new Donation();
                    donation.setDevoteeName(devotee);
                    donation.setOccasion(occasion);
                    donation.setAmount(BigDecimal.valueOf(amount));
                    donation.setDate(LocalDate.of(year, month, day));
                    donation.setMobile("9" + mobileBase);
                    sheet.addDonation(donation);
                }
            }

            sheet.getDonations().sort(Comparator.comparing(Donation::getDate));
            sheetRepository.save(sheet);
        }
    }

    private void seedUsers() {
        if (userRepository.existsByUsernameIgnoreCase("admin")) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setDisplayName("Administrator");
        userRepository.save(admin);
    }

    private static final Map<String, List<String>> OCCASION_SETS = Map.ofEntries(
            Map.entry("Soumyaranjan Narendra",
                    List.of("Pongal", "Maha Shivaratri", "Deepavali", "Karthigai Deepam")),
            Map.entry("Subhrakanta Narendra",
                    List.of("Pongal", "Arudhra Darshan", "Vinayaka Chaturthi", "Friday Special Abhishekam")),
            Map.entry("Sibakumar Narendra",
                    List.of("Deepavali", "Navaratri", "Annual Festival", "General Donation")),
            Map.entry("Prakash Behera",
                    List.of("Tamil New Year", "Panguni Uthiram", "Monthly Special Pooja")),
            Map.entry("Manas Rout",
                    List.of("Friday Special Abhishekam", "Deepavali", "Pongal")),
            Map.entry("Rajesh Kumar",
                    List.of("Pongal", "Deepavali", "Maha Shivaratri")),
            Map.entry("Anita Sahoo",
                    List.of("Navaratri", "Friday Special Abhishekam", "Karthigai Deepam")),
            Map.entry("Bikash Nayak",
                    List.of("Annual Festival", "Panguni Uthiram", "General Donation")),
            Map.entry("Sushree Panda",
                    List.of("Tamil New Year", "Arudhra Darshan", "Vinayaka Chaturthi")),
            Map.entry("Debasish Dash",
                    List.of("Deepavali", "Monthly Special Pooja", "Pongal")),
            Map.entry("Jyoti Ranjan",
                    List.of("Maha Shivaratri", "Karthigai Deepam", "Panguni Uthiram")),
            Map.entry("Pratap Mohanty",
                    List.of("Friday Special Abhishekam", "Navaratri", "General Donation")),
            Map.entry("Sunita Dei",
                    List.of("Pongal", "Vinayaka Chaturthi", "Annual Festival")),
            Map.entry("Gopal Chandra",
                    List.of("Deepavali", "Arudhra Darshan", "Monthly Special Pooja")),
            Map.entry("Sasmita Behera",
                    List.of("Navaratri", "Tamil New Year", "Karthigai Deepam")));
}
