package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author barea
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {

        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {

            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }

        } catch (IOException e) {
            logger.error("Failed to load the sensitive word file: " + e.getMessage());
        }

    }

    private void addKeyword(String keyword) {

        TrieNode tempNode = rootNode;

        for (int i = 0; i < keyword.length(); i++) {

            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {

                //Initial the child node
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);

            }

            //Update current node
            tempNode = subNode;

            //Mark the ending flag
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }

        }

    }

    /**
     * This method is copied from the assistant teacher's post
     * from the classroom forum. The one from the lecturer is WRONG.
     *
     * This method takes a text and censors it.
     * @param text The original text.
     * @return The text after censorship.
     */
    public String filter(String text) {

        if (StringUtils.isBlank(text)) {
            return null;
        }
        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (begin < text.length()) {

            if (position < text.length()) {

                Character c = text.charAt(position);

                // 跳过符号
                if (isSymbol(c)) {

                    if (tempNode == rootNode) {

                        begin++;
                        sb.append(c);

                    }

                    position++;
                    continue;

                }

                // 检查下级节点
                tempNode = tempNode.getSubNode(c);
                if (tempNode == null) {

                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = rootNode;

                }
                // 发现敏感词
                else if (tempNode.isKeywordEnd()) {

                    sb.append(REPLACEMENT);
                    begin = ++position;

                }
                // 检查下一个字符
                else {
                    position++;
                }

            }
            // position遍历越界仍未匹配到敏感词
            else {

                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;

            }

        }

        return sb.toString();

    }

    private boolean isSymbol(Character c) {
        // 0x2E80 to 0x9FFF is the scope for East Asian characters
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private class TrieNode {

        //If the ending character of a sensitive word
        private boolean isKeywordEnd = false;

        //Child trie nodes
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //Add a child node
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //Get a specific child
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }

}
