package com.makersacademy.acebook.model;

import java.util.ArrayList;
import java.util.List;

public class UserSearch {
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(
                            s1.charAt(i - 1), s2.charAt(j - 1)), dp[i - 1][j] + 1, dp[i][j - 1] + 1);
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private int min(int x, int y, int z) {
        return Math.min(Math.min(x, y), z);
    }

    public List<User> searchUsers(Iterable<User> users, String searchText) {
        List<User> filteredUsers = new ArrayList<>();
        String searchTextLower = searchText.toLowerCase();
        for (User user : users) {
            int distanceFirstName = calculateLevenshteinDistance(searchTextLower, user.getFirstname().toLowerCase());
            int distanceLastName = calculateLevenshteinDistance(searchTextLower, user.getLastname().toLowerCase());
            int distanceUsername = calculateLevenshteinDistance(searchTextLower, user.getUsername().toLowerCase());
            int distanceEmail = calculateLevenshteinDistance(searchTextLower, user.getEmail().toLowerCase());

            // Consider a user a match if any of the distances are below a certain threshold
            if (distanceFirstName <= 2 || distanceLastName <= 2 || distanceUsername <= 2 || distanceEmail <= 2) {
                filteredUsers.add(user);
            }
        }

        return filteredUsers;
    }
}
