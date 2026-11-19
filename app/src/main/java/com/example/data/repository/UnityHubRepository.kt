package com.example.data.repository

import com.example.data.local.TutorialEntity
import com.example.data.local.UnityHubDao
import com.example.data.local.UnityProjectEntity
import kotlinx.coroutines.flow.Flow

object UnityWebUrls {
    const val BRANDING_PT = "https://unity.com/pt/legal/branding-trademarks"
    const val BRANDING_EN = "https://unity.com/legal/branding-trademarks"
    const val UNITY_LEARN = "https://learn.unity.com"
}

class UnityHubRepository(private val dao: UnityHubDao) {

    val projects: Flow<List<UnityProjectEntity>> = dao.observeProjects()
    val tutorials: Flow<List<TutorialEntity>> = dao.observeTutorials()

    suspend fun ensureSeeded() {
        val now = System.currentTimeMillis()
        if (dao.getProjectCount() == 0) {
            dao.insertProjects(
                listOf(
                    UnityProjectEntity(
                        name = "Neon Horizon URP",
                        localPath = "~/UnityProjects/NeonHorizon",
                        template = "3D (URP)",
                        editorVersion = "6000.0.28f1 LTS",
                        targetPlatform = "Android",
                        isFavorite = true,
                        lastModifiedEpochMs = now - 3_600_000L,
                        description = "High-frame-rate cyberpunk sci-fi environment using Universal Render Pipeline and adaptive GPU Instancing.",
                        sceneCount = 6
                    ),
                    UnityProjectEntity(
                        name = "Pixel Odyssey 2D",
                        localPath = "~/UnityProjects/PixelOdyssey2D",
                        template = "2D (Built-in)",
                        editorVersion = "2022.3.51f1 LTS",
                        targetPlatform = "Android",
                        isFavorite = true,
                        lastModifiedEpochMs = now - 14_400_000L,
                        description = "Side-scrolling platformer featuring Pixel Perfect Camera, Tilemap collisions, and Input System bindings.",
                        sceneCount = 9
                    ),
                    UnityProjectEntity(
                        name = "Spatial XR Lab",
                        localPath = "~/UnityProjects/SpatialXRLab",
                        template = "VR Core",
                        editorVersion = "6000.0.28f1 LTS",
                        targetPlatform = "Meta Quest XR",
                        isFavorite = false,
                        lastModifiedEpochMs = now - 86_400_000L,
                        description = "Hand-tracked spatial interaction prototype built with XR Interaction Toolkit 3.0 and OpenXR.",
                        sceneCount = 4
                    ),
                    UnityProjectEntity(
                        name = "Shader Graph Studio",
                        localPath = "~/UnityProjects/ShaderGraphStudio",
                        template = "3D (URP)",
                        editorVersion = "6000.0.28f1 LTS",
                        targetPlatform = "Windows / Mac",
                        isFavorite = false,
                        lastModifiedEpochMs = now - 172_800_000L,
                        description = "Collection of stylized water, dissolve, and hologram custom Shader Graph materials for mobile GPUs.",
                        sceneCount = 12
                    ),
                    UnityProjectEntity(
                        name = "Pocket Arena Mobile",
                        localPath = "~/UnityProjects/PocketArenaMobile",
                        template = "Mobile 3D",
                        editorVersion = "2022.3.51f1 LTS",
                        targetPlatform = "Android",
                        isFavorite = false,
                        lastModifiedEpochMs = now - 432_000_000L,
                        description = "Lightweight top-down arena starter configured with ASTC texture compression and IL2CPP ARM64.",
                        sceneCount = 5
                    )
                )
            )
        }

        if (dao.getTutorialCount() == 0) {
            dao.insertTutorials(
                listOf(
                    TutorialEntity(
                        title = "Unity Branding & Trademarks Guide",
                        category = "Branding & Legal",
                        difficulty = "Beginner",
                        durationMinutes = 10,
                        summary = "Learn official rules for using the Unity cube logo, wordmark clear space, splash screens, and attribution on websites and games.",
                        stepsDelimited = """
                            Review official Unity trademark attribution guidelines
                            Verify minimum clear space around the Unity logo mark
                            Check approved monochrome white and black toolbar usage
                            Confirm 'Made with Unity' splash and marketing rules
                        """.trimIndent(),
                        completedStepsBitmask = 1,
                        isBookmarked = true,
                        websiteUrl = UnityWebUrls.BRANDING_EN
                    ),
                    TutorialEntity(
                        title = "Diretrizes de Marca Unity (PT)",
                        category = "Branding & Legal",
                        difficulty = "Beginner",
                        durationMinutes = 10,
                        summary = "Guia oficial em português sobre uso de marcas registradas, logotipos Unity, espaçamento mínimo e diretrizes legais.",
                        stepsDelimited = """
                            Ler termos de uso da marca Unity em português
                            Validar proporções e contraste do logotipo em fundos claros
                            Revisar regras de uso em títulos e descrições de aplicativos
                        """.trimIndent(),
                        completedStepsBitmask = 0,
                        isBookmarked = true,
                        websiteUrl = UnityWebUrls.BRANDING_PT
                    ),
                    TutorialEntity(
                        title = "Unity 6 URP Mobile Lighting",
                        category = "3D & URP",
                        difficulty = "Intermediate",
                        durationMinutes = 25,
                        summary = "Configure Universal Render Pipeline Forward+ lights, baked GI lightmaps, and post-processing for 60 FPS Android builds.",
                        stepsDelimited = """
                            Create a URP Asset tuned for mobile MSAA 2x
                            Bake global illumination with Progressive GPU Lightmapper
                            Set up Reflection Probes and Light Probe Groups
                            Profile frame timings using Frame Debugger
                        """.trimIndent(),
                        completedStepsBitmask = 3,
                        isBookmarked = false,
                        websiteUrl = UnityWebUrls.UNITY_LEARN
                    ),
                    TutorialEntity(
                        title = "2D Platformer Physics & Input",
                        category = "2D & Physics",
                        difficulty = "Beginner",
                        durationMinutes = 20,
                        summary = "Build responsive jump buffering, coyote time, and Sprite Atlas animations using Rigidbody2D and the new Input System.",
                        stepsDelimited = """
                            Configure Tilemap Collider 2D with Composite Collider
                            Create an Input Actions asset for touch and gamepad
                            Implement ground check raycasts and coyote time
                            Pack sprites into a Sprite Atlas for single draw call
                        """.trimIndent(),
                        completedStepsBitmask = 0,
                        isBookmarked = false,
                        websiteUrl = UnityWebUrls.UNITY_LEARN
                    ),
                    TutorialEntity(
                        title = "C# Async & ScriptableObjects",
                        category = "Scripting",
                        difficulty = "Advanced",
                        durationMinutes = 30,
                        summary = "Decouple game systems using ScriptableObject event channels, Awaitable coroutines in Unity 6, and Addressables.",
                        stepsDelimited = """
                            Design modular ScriptableObject configuration assets
                            Replace legacy StartCoroutine loops with Unity 6 Awaitable
                            Bind UI Toolkit elements to reactive data containers
                        """.trimIndent(),
                        completedStepsBitmask = 0,
                        isBookmarked = false,
                        websiteUrl = UnityWebUrls.UNITY_LEARN
                    )
                )
            )
        }
    }

    suspend fun createProject(
        name: String,
        template: String,
        editorVersion: String,
        targetPlatform: String,
        description: String
    ) {
        val sanitizedSlug = name.trim().replace(Regex("[^A-Za-z0-9]"), "").ifEmpty { "NewUnityProject" }
        dao.insertProject(
            UnityProjectEntity(
                name = name.trim(),
                localPath = "~/UnityProjects/$sanitizedSlug",
                template = template,
                editorVersion = editorVersion,
                targetPlatform = targetPlatform,
                isFavorite = false,
                lastModifiedEpochMs = System.currentTimeMillis(),
                description = description.ifBlank { "Created in Unity Hub with $template template targeting $targetPlatform." },
                sceneCount = 1
            )
        )
    }

    suspend fun toggleProjectFavorite(project: UnityProjectEntity) {
        dao.updateProject(project.copy(isFavorite = !project.isFavorite))
    }

    suspend fun updateProjectEditorAndTarget(
        project: UnityProjectEntity,
        editorVersion: String,
        targetPlatform: String
    ) {
        dao.updateProject(
            project.copy(
                editorVersion = editorVersion,
                targetPlatform = targetPlatform,
                lastModifiedEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun touchProjectOpened(project: UnityProjectEntity) {
        dao.updateProject(project.copy(lastModifiedEpochMs = System.currentTimeMillis()))
    }

    suspend fun deleteProject(projectId: Int) {
        dao.deleteProjectById(projectId)
    }

    suspend fun toggleTutorialStep(tutorial: TutorialEntity, stepIndex: Int) {
        val updatedMask = tutorial.completedStepsBitmask xor (1 shl stepIndex)
        dao.updateTutorial(tutorial.copy(completedStepsBitmask = updatedMask))
    }

    suspend fun toggleTutorialBookmark(tutorial: TutorialEntity) {
        dao.updateTutorial(tutorial.copy(isBookmarked = !tutorial.isBookmarked))
    }
}
