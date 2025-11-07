/**
 * Skill Activation Hook (UserPromptSubmit)
 * 
 * 这是整个系统的核心 - 自动技能激活！
 * 
 * 功能：
 * 1. 在每次用户提交问题前运行
 * 2. 检查 skill-rules.json 中的触发规则
 * 3. 自动建议相关的技能
 * 
 * 工作原理：
 * - 检查最近编辑的文件路径
 * - 检查用户的问题内容
 * - 匹配 skill-rules.json 中的规则
 * - 返回建议激活的技能
 */

const fs = require('fs');
const path = require('path');

module.exports = async (claudeAPI) => {
  const { userPrompt, recentlyEditedFiles } = claudeAPI;
  
  try {
    // 读取技能规则配置
    const rulesPath = path.join(process.cwd(), '.claude', 'skill-rules.json');
    if (!fs.existsSync(rulesPath)) {
      return null; // 如果没有规则文件，直接返回
    }
    
    const rules = JSON.parse(fs.readFileSync(rulesPath, 'utf-8'));
    const suggestedSkills = new Set();
    
    // 检查最近编辑的文件
    if (recentlyEditedFiles && recentlyEditedFiles.length > 0) {
      for (const file of recentlyEditedFiles) {
        for (const [skillName, config] of Object.entries(rules.skills)) {
          if (!config.enabled) continue;
          
          // 检查文件路径匹配
          if (config.filePatterns) {
            for (const pattern of config.filePatterns) {
              if (file.includes(pattern)) {
                suggestedSkills.add(skillName);
              }
            }
          }
        }
      }
    }
    
    // 检查用户提示内容
    if (userPrompt) {
      const lowerPrompt = userPrompt.toLowerCase();
      
      for (const [skillName, config] of Object.entries(rules.skills)) {
        if (!config.enabled) continue;
        
        // 检查关键词匹配
        if (config.keywords) {
          for (const keyword of config.keywords) {
            if (lowerPrompt.includes(keyword.toLowerCase())) {
              suggestedSkills.add(skillName);
            }
          }
        }
      }
    }
    
    // 如果有建议的技能，生成提示
    if (suggestedSkills.size > 0) {
      const skillsList = Array.from(suggestedSkills)
        .map(skill => {
          const config = rules.skills[skill];
          return `- **${skill}**: ${config.description || ''}`;
        })
        .join('\n');
      
      return {
        prependToUserPrompt: `
[系统提示] 根据你的问题和编辑的文件，以下技能可能有帮助：

${skillsList}

如果需要，请考虑激活相关技能。使用 @${Array.from(suggestedSkills)[0]} 来激活。

---

用户问题：
`
      };
    }
    
    return null;
  } catch (error) {
    console.error('Skill activation hook error:', error);
    return null;
  }
};

